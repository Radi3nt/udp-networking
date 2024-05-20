package fr.radi3nt.udp.message.recievers.local;

import fr.radi3nt.udp.message.recievers.BufferPacketFrameReceiver;
import fr.radi3nt.udp.message.recievers.PacketFrameReceiver;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class LocalPacketFrameReceiver extends BufferPacketFrameReceiver implements PacketFrameReceiver, Consumer<ByteBuffer> {

    private final Queue<ByteBuffer> receivedMessages = new ConcurrentLinkedQueue<>();

    public LocalPacketFrameReceiver(int maxDatagramSize) {
        super(maxDatagramSize);
    }

    public void received(ByteBuffer message) {
        receivedMessages.add(message);
    }

    @Override
    public void accept(ByteBuffer buffer) {
        received(buffer);
    }

    @Override
    protected int read(ByteBuffer currentBuffer) {
        ByteBuffer poll = receivedMessages.poll();
        if (poll==null)
            return 0;
        int remaining = poll.remaining();
        currentBuffer.put(poll);
        return remaining;
    }
}
