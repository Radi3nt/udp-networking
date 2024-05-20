package fr.radi3nt.udp.message.senders;

import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.PacketMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class EncodingPacketFrameSender implements PacketFrameSender {

    private final Queue<PacketFrame> frames = new ConcurrentLinkedQueue<>();
    private final FramesToMessageAssembler assembler;

    public EncodingPacketFrameSender(int packetSizeLimit) {
        this.assembler = new FramesToMessageAssembler(packetSizeLimit, this::tryConsumeFragment);
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
        packetMessage.encode(buffer);

        buffer.flip();
        System.out.println("sending");
        int writtenBytes = write(buffer);
        System.out.println("sent " + buffer.limit() + "/" + writtenBytes + " bytes");
    }

    protected abstract int write(ByteBuffer buffer) throws IOException;
}
