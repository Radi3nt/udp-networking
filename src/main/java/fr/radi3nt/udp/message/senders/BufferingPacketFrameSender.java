package fr.radi3nt.udp.message.senders;

import fr.radi3nt.udp.message.PacketMessage;
import fr.radi3nt.udp.message.PacketFrame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BufferingPacketFrameSender implements PacketFrameSender {

    private final Queue<PacketFrame> frames = new ConcurrentLinkedQueue<>();
    private final DatagramChannel channel;
    private final FramesToFragmentAssembler assembler;

    public BufferingPacketFrameSender(DatagramChannel channel, int packetSizeLimit) {
        this.channel = channel;
        this.assembler = new FramesToFragmentAssembler(packetSizeLimit, this::tryConsumeFragment);
    }

    @Override
    public void addFrame(PacketFrame frame) {
        frames.add(frame);
    }

    @Override
    public void sendFrames() {
        assembler.assemble(frames);
    }

    private void tryConsumeFragment(PacketMessage packetMessage) {
        try {
            consumeFragment(packetMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void consumeFragment(PacketMessage packetMessage) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(packetMessage.totalSize);
        for (PacketFrame frame : packetMessage.frames) {
            buffer.put(frame.data);
        }

        buffer.flip();
        System.out.println("sending");
        int writtenBytes = channel.write(buffer);
        System.out.println("sent " + buffer.limit() + "/" + writtenBytes + " bytes");
    }

}
