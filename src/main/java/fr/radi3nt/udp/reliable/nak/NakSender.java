package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.IncompleteFragments;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameHeader;
import fr.radi3nt.udp.message.frame.FrameType;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;

import static fr.radi3nt.udp.message.frame.FrameHeader.HEADER_SIZE_BYTES;

public class NakSender {

    private static final int MESSAGE_PACKET_HEADER_BYTES = 2*Long.BYTES + Integer.BYTES;
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

    public void request(long streamId, Collection<IncompleteFragments> fragments, long minTermId) {
        Collection<IncompleteFragments> relevantFragments = Collections.emptyList();

        if (!fragments.isEmpty()) {
            relevantFragments =new ArrayList<>(fragments);
            relevantFragments.removeIf((f) -> f.resendingNotNeeded(activeTimeout, inactiveTimeout));
        }

        boolean empty = relevantFragments.isEmpty();

        if (empty) {
            if (System.currentTimeMillis()-lastSent<=1000)
                return;

            lastSent=System.currentTimeMillis();

            sendComplete(streamId, 0, Collections.EMPTY_LIST, minTermId);
            return;
        }

        int currentSize = 0;
        Collection<ByteArrayOutputStream> doneSteams = new ArrayList<>();

        for (IncompleteFragments fragment : relevantFragments) {
            fragment.sent();

            BitSet currentSet = fragment.receivedFragmentsBits;
            long termId = fragment.termId;

            int setByteArrayLength = (currentSet.length()+7)/8;
            int remainingSizeForSet = totalSize-MESSAGE_PACKET_HEADER_BYTES-MISSING_FRAGMENT_HEADER_BYTES;
            if (setByteArrayLength>remainingSizeForSet) {
                int splitAmount = (setByteArrayLength/remainingSizeForSet);
                int setBytesToSetLength = remainingSizeForSet*8-7;

                for (int i = 0; i < splitAmount; i++) {
                    int maxBound = Math.min(currentSet.length(), (i+1)*setBytesToSetLength);
                    BitSet splitSet = currentSet.get(i * setBytesToSetLength, maxBound);
                    if (splitSet.cardinality()==splitSet.length()) {
                        continue;
                    }
                    currentSize = encodeAndPossiblySend(streamId, splitSet, termId, currentSize, doneSteams, minTermId, i*setBytesToSetLength, i==splitAmount-1 ? Integer.MAX_VALUE : maxBound);
                }
            } else {
                currentSize = encodeAndPossiblySend(streamId, currentSet, termId, currentSize, doneSteams, minTermId, 0, Integer.MAX_VALUE);
            }
        }

        sendComplete(streamId, currentSize, doneSteams, minTermId);

    }

    private int encodeAndPossiblySend(long streamId, BitSet currentSet, long termId, int currentSize, Collection<ByteArrayOutputStream> doneSteams, long minTermId, int offset, int ends) {
        byte[] array;
        array = currentSet.toByteArray();

        ByteBuffer header = ByteBuffer.allocate(MISSING_FRAGMENT_HEADER_BYTES);
        encodeFragmentHeader(termId, array.length, header, offset, ends);

        ByteArrayOutputStream completedFragment = new ByteArrayOutputStream();
        completedFragment.write(header.array(), 0, header.position());
        completedFragment.write(array, 0, array.length);

        currentSize = sendAlreadyEncodedIfTooLargeToAddNew(streamId, currentSize, completedFragment, doneSteams, minTermId);

        currentSize += completedFragment.size();

        if (currentSize>totalSize)
            System.out.println("current packet size is bigger then what we can send, what happened?");

        doneSteams.add(completedFragment);
        return currentSize;
    }

    private int sendAlreadyEncodedIfTooLargeToAddNew(long streamId, int currentSize, ByteArrayOutputStream completedFragment, Collection<ByteArrayOutputStream> doneSteams, long minTermId) {
        if (currentSize!=0 && currentSize + completedFragment.size()+ MESSAGE_PACKET_HEADER_BYTES >totalSize) {
            sendComplete(streamId, currentSize, doneSteams, minTermId);
            doneSteams.clear();
            currentSize = 0;
        }
        return currentSize;
    }

    private void sendComplete(long streamId, int currentSize, Collection<ByteArrayOutputStream> doneSteams, long minTermId) {
        ByteBuffer currentMessage = ByteBuffer.allocate(MESSAGE_PACKET_HEADER_BYTES + currentSize);

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

    private static void encodeFragmentHeader(long termId, int encodedMessages, ByteBuffer currentMessage, int start, int end) {
        currentMessage.putLong(termId);
        currentMessage.putInt(encodedMessages);
        currentMessage.putInt(start);
        currentMessage.putInt(end);
    }

}
