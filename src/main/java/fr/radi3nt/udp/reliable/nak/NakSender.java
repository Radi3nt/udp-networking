package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.IncompleteFragments;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameHeader;
import fr.radi3nt.udp.message.frame.FrameType;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

import static fr.radi3nt.udp.data.streams.FragmentingPacketStream.LAST_MESSAGE_HINT;
import static fr.radi3nt.udp.message.frame.FrameHeader.HEADER_SIZE_BYTES;

public class NakSender {

    private static final int IDEAL_BIT_SET_SIZE = 512-20-20-HEADER_SIZE_BYTES;
    private static final int MAX_BITS = (IDEAL_BIT_SET_SIZE*8-7)/8;

    private static final int MESSAGE_ADDITIONAL_DATA_BYTES = 2*Long.BYTES + Integer.BYTES;
    private static final int MISSING_FRAGMENT_HEADER_BYTES = Long.BYTES + 3*Integer.BYTES;
    private final PacketFrameSender frameSender;
    private final int totalSize;

    private long lastSent = System.currentTimeMillis();
    private final int activeTimeout;
    private final int inactiveTimeout;

    public NakSender(PacketFrameSender frameSender, int totalSize, int activeTimeout, int inactiveTimeout) {
        this.frameSender = frameSender;
        this.totalSize = totalSize-HEADER_SIZE_BYTES;
        this.activeTimeout = activeTimeout;
        this.inactiveTimeout = inactiveTimeout;
    }

    public void request(long streamId, Collection<IncompleteFragments> fragments, long currentTerm) {
        Collection<IncompleteFragments> relevantFragments = new ArrayList<>(fragments);
        relevantFragments.removeIf((f) -> f.resendingNotNeeded(activeTimeout, inactiveTimeout));

        boolean empty = relevantFragments.isEmpty();

        if (empty) {
            if (System.currentTimeMillis()-lastSent<=1000)
                return;

            lastSent=System.currentTimeMillis();

            sendComplete(streamId, 0, Collections.EMPTY_LIST, currentTerm-1);
            return;
        }

        long minTermId = Long.MAX_VALUE;
        for (IncompleteFragments fragment : fragments) {
            minTermId = Math.min(fragment.termId, minTermId);
        }

        int currentSize = 0;
        Collection<ByteArrayOutputStream> doneSteams = new ArrayList<>();

        int estimatedSize;

        for (IncompleteFragments fragment : relevantFragments) {
            fragment.sent();

            BitSet currentSet = fragment.receivedFragmentsBits;
            long termId = fragment.termId;

            int notSetBits = currentSet.length() - currentSet.cardinality();
            int supposedBitSetBytesArrayLength = (currentSet.length()+7)/8;
            boolean sendingArray = notSetBits*Integer.BYTES+Integer.BYTES < supposedBitSetBytesArrayLength;

            estimatedSize = (sendingArray ? notSetBits*Integer.BYTES+Integer.BYTES : supposedBitSetBytesArrayLength)+MESSAGE_ADDITIONAL_DATA_BYTES+MISSING_FRAGMENT_HEADER_BYTES;

            if (estimatedSize>totalSize) {
                int split = (int) Math.ceil((float) estimatedSize/totalSize);
                for (int i = 0; i < split; i++) {
                    currentSize = encodeAndPossiblySend(streamId, currentSet.get(i*MAX_BITS, (i+1)*MAX_BITS), termId, currentSize, doneSteams, minTermId, i*MAX_BITS, (i+1)*MAX_BITS);
                }
            } else {
                currentSize = encodeAndPossiblySend(streamId, currentSet, termId, currentSize, doneSteams, minTermId, 0, Integer.MAX_VALUE);
            }

        }

        sendComplete(streamId, currentSize, doneSteams, minTermId);

    }

    private int encodeAndPossiblySend(long streamId, BitSet currentSet, long termId, int currentSize, Collection<ByteArrayOutputStream> doneSteams, long minTermId, int offset, int ends) {
        int notSetBits = currentSet.length() - currentSet.cardinality();
        int supposedBitSetBytesArrayLength = (currentSet.length()+7)/8;
        boolean sendingArray = notSetBits*Integer.BYTES+Integer.BYTES < supposedBitSetBytesArrayLength;

        byte[] array;
        if (sendingArray) {
            ByteBuffer buffer = ByteBuffer.allocate(notSetBits *Integer.BYTES+Integer.BYTES);

            buffer.putInt(currentSet.length());

            int pos = 0;
            while ((pos = currentSet.nextClearBit(pos))< currentSet.length()) {
                buffer.putInt(pos);
                pos++;
            }

            array = buffer.array();
        } else {
            array = currentSet.toByteArray();
        }

        ByteBuffer header = ByteBuffer.allocate(MISSING_FRAGMENT_HEADER_BYTES);
        encodeFragmentHeader(termId, array.length, sendingArray, header, offset, ends);

        ByteArrayOutputStream completedFragment = new ByteArrayOutputStream();
        completedFragment.write(header.array(), 0, header.position());
        completedFragment.write(array, 0, array.length);

        currentSize = sendAlreadyEncodedIfTooLargeToAddNew(streamId, currentSize, completedFragment, doneSteams, minTermId);

        currentSize += completedFragment.size();

        if (currentSize>totalSize)
            System.out.println("what?");

        doneSteams.add(completedFragment);
        return currentSize;
    }

    private int sendAlreadyEncodedIfTooLargeToAddNew(long streamId, int currentSize, ByteArrayOutputStream completedFragment, Collection<ByteArrayOutputStream> doneSteams, long minTermId) {
        if (currentSize!=0 && currentSize + completedFragment.size()+MESSAGE_ADDITIONAL_DATA_BYTES>totalSize) {
            sendComplete(streamId, currentSize, doneSteams, minTermId);
            doneSteams.clear();
            currentSize = 0;
        }
        return currentSize;
    }

    private void sendComplete(long streamId, int currentSize, Collection<ByteArrayOutputStream> doneSteams, long minTermId) {
        ByteBuffer currentMessage = ByteBuffer.allocate(MESSAGE_ADDITIONAL_DATA_BYTES + currentSize);

        writePacketHeader(streamId, doneSteams, currentMessage);

        for (ByteArrayOutputStream doneSteam : doneSteams) {
            currentMessage.put(doneSteam.toByteArray());
        }

        currentMessage.putLong(minTermId);
        currentMessage.flip();

        if (currentMessage.limit()>totalSize) {
            System.err.println("NAK message is exceeding the limit (" + currentMessage.limit() + "/" + (totalSize+HEADER_SIZE_BYTES) + "), not sending");
            return;
        }

        frameSender.addFrame(new PacketFrame(new FrameHeader(FrameType.NAK, currentMessage.limit()), currentMessage.array()));
    }

    private static void writePacketHeader(long streamId, Collection<ByteArrayOutputStream> doneSteams, ByteBuffer currentMessage) {
        currentMessage.putLong(streamId);
        currentMessage.putInt(doneSteams.size());
    }

    private static void encodeFragmentHeader(long termId, int encodedMessages, boolean sendingArray, ByteBuffer currentMessage, int start, int end) {
        currentMessage.putLong(termId);
        currentMessage.putInt(sendingArray ? (encodedMessages|LAST_MESSAGE_HINT) : encodedMessages);
        currentMessage.putInt(start);
        currentMessage.putInt(end);
    }

}
