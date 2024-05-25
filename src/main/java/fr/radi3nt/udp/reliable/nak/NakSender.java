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

public class NakSender {

    private static final int MESSAGE_ADDITIONAL_DATA_BYTES = 2*Long.BYTES + Integer.BYTES;
    private static final int MISSING_FRAGMENT_HEADER_BYTES = Long.BYTES + 2*Integer.BYTES;
    private final PacketFrameSender frameSender;
    private final int totalSize;

    public NakSender(PacketFrameSender frameSender, int totalSize) {
        this.frameSender = frameSender;
        this.totalSize = totalSize;
    }

    public void request(long streamId, Collection<MissingFragments> fragments) {
        if (fragments==null || fragments.isEmpty())
            return;

        long minTermId = Long.MAX_VALUE;
        for (MissingFragments fragment : fragments) {
            minTermId = Math.min(fragment.termId, minTermId);
        }

        int currentSize = 0;
        Collection<ByteArrayOutputStream> doneSteams = new ArrayList<>();

        ByteBuffer messages = ByteBuffer.allocate(totalSize);
        for (MissingFragments fragment : fragments) {

            int encodedMessages = 0;
            for (int missingFragment : fragment.missingFragments) {
                if (currentSize+messages.position()+Integer.BYTES+MESSAGE_ADDITIONAL_DATA_BYTES+MISSING_FRAGMENT_HEADER_BYTES>totalSize) {
                    messages.flip();
                    sendIncomplete(streamId, fragment, currentSize, messages, doneSteams, encodedMessages, minTermId);
                    doneSteams.clear();
                    currentSize = 0;
                    encodedMessages=0;

                }
                messages.putInt(missingFragment);
                encodedMessages++;
            }

            ByteBuffer header = ByteBuffer.allocate(MISSING_FRAGMENT_HEADER_BYTES);
            encodeFragmentHeader(fragment, encodedMessages, header);

            ByteArrayOutputStream completedFragment = new ByteArrayOutputStream();
            completedFragment.write(header.array(), 0, header.position());
            completedFragment.write(messages.array(), 0, messages.position());

            if (currentSize+completedFragment.size()+MESSAGE_ADDITIONAL_DATA_BYTES>totalSize) {
                sendComplete(streamId, currentSize, doneSteams, minTermId);
                doneSteams.clear();
                currentSize = 0;
            }

            currentSize+=completedFragment.size();
            doneSteams.add(completedFragment);
        }

        sendComplete(streamId, currentSize, doneSteams, minTermId);

    }

    private void sendComplete(long streamId, int currentSize, Collection<ByteArrayOutputStream> doneSteams, long minTermId) {
        ByteBuffer currentMessage = ByteBuffer.allocate(MESSAGE_ADDITIONAL_DATA_BYTES + currentSize);

        writePacketHeader(streamId, doneSteams, currentMessage, 0);

        for (ByteArrayOutputStream doneSteam : doneSteams) {
            currentMessage.put(doneSteam.toByteArray());
        }

        currentMessage.putLong(minTermId);
        currentMessage.flip();

        if (currentMessage.limit()>512)
            System.out.println("not?");

        //System.out.println("resending missing:" + fragments);

        frameSender.addFrame(new PacketFrame(new FrameHeader(FrameType.NAK, currentMessage.limit()), currentMessage.array()));
    }

    private void sendIncomplete(long streamId, MissingFragments fragment, int currentSize, ByteBuffer byteStream, Collection<ByteArrayOutputStream> doneSteams, int encodedMessages, long minTermId) {
        ByteBuffer currentMessage = ByteBuffer.allocate(MESSAGE_ADDITIONAL_DATA_BYTES+MISSING_FRAGMENT_HEADER_BYTES + currentSize + byteStream.limit());

        writePacketHeader(streamId, doneSteams, currentMessage, 1);

        for (ByteArrayOutputStream doneSteam : doneSteams) {
            currentMessage.put(doneSteam.toByteArray());
        }

        encodeFragmentHeader(fragment, encodedMessages, currentMessage);
        currentMessage.put(byteStream.array(), 0, byteStream.limit());

        currentMessage.putLong(minTermId);
        currentMessage.flip();

        if (currentMessage.limit()>512)
            System.out.println("not?");
            //System.out.println("resending missing:" + fragments);

        frameSender.addFrame(new PacketFrame(new FrameHeader(FrameType.NAK, currentMessage.limit()), currentMessage.array()));
    }

    private static void writePacketHeader(long streamId, Collection<ByteArrayOutputStream> doneSteams, ByteBuffer currentMessage, int added) {
        currentMessage.putLong(streamId);
        currentMessage.putInt(doneSteams.size()+added);
    }

    private static void encodeFragmentHeader(MissingFragments fragment, int encodedMessages, ByteBuffer currentMessage) {
        currentMessage.putLong(fragment.termId);
        currentMessage.putInt(fragment.lastReceivedOffset);
        currentMessage.putInt(encodedMessages);
    }

}
