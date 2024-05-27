package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.MissingFragments;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameHeader;
import fr.radi3nt.udp.message.frame.FrameType;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static fr.radi3nt.udp.message.frame.FrameHeader.HEADER_SIZE_BYTES;

public class NakSender {

    private static final int MESSAGE_ADDITIONAL_DATA_BYTES = 2*Long.BYTES + Integer.BYTES;
    private static final int MISSING_FRAGMENT_HEADER_BYTES = Long.BYTES + Integer.BYTES;
    private final PacketFrameSender frameSender;
    private final int totalSize;

    private long lastSent = System.currentTimeMillis();

    public NakSender(PacketFrameSender frameSender, int totalSize) {
        this.frameSender = frameSender;
        this.totalSize = totalSize-HEADER_SIZE_BYTES;
    }

    public void request(long streamId, Collection<MissingFragments> fragments, long currentTerm) {
        boolean empty = (fragments==null || fragments.isEmpty());

        if (empty) {
            if (System.currentTimeMillis()-lastSent<=1000)
                return;

            lastSent=System.currentTimeMillis();

            sendComplete(streamId, 0, Collections.EMPTY_LIST, currentTerm-1);
            return;
        }

        long minTermId = Long.MAX_VALUE;
        for (MissingFragments fragment : fragments) {
            minTermId = Math.min(fragment.termId, minTermId);
        }

        int currentSize = 0;
        Collection<ByteArrayOutputStream> doneSteams = new ArrayList<>();

        for (MissingFragments fragment : fragments) {
            byte[] array = fragment.receivedFragmentsBits.toByteArray();

            ByteBuffer header = ByteBuffer.allocate(MISSING_FRAGMENT_HEADER_BYTES);
            encodeFragmentHeader(fragment, array.length, header);

            ByteArrayOutputStream completedFragment = new ByteArrayOutputStream();
            completedFragment.write(header.array(), 0, header.position());
            completedFragment.write(array, 0, array.length);

            currentSize = sendAlreadyEncodedIfTooLargeToAddNew(streamId, currentSize, completedFragment, doneSteams, minTermId);

            currentSize += completedFragment.size();
            doneSteams.add(completedFragment);
        }

        sendComplete(streamId, currentSize, doneSteams, minTermId);

    }

    private int sendAlreadyEncodedIfTooLargeToAddNew(long streamId, int currentSize, ByteArrayOutputStream completedFragment, Collection<ByteArrayOutputStream> doneSteams, long minTermId) {
        if (currentSize!=0 && currentSize + completedFragment.size()+MESSAGE_ADDITIONAL_DATA_BYTES+MISSING_FRAGMENT_HEADER_BYTES+Integer.BYTES>totalSize) {
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

    private static void encodeFragmentHeader(MissingFragments fragment, int encodedMessages, ByteBuffer currentMessage) {
        currentMessage.putLong(fragment.termId);
        currentMessage.putInt(encodedMessages);
    }

}
