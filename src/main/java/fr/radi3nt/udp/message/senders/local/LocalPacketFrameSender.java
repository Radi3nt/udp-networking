package fr.radi3nt.udp.message.senders.local;

import fr.radi3nt.udp.message.senders.EncodingPacketFrameSender;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class LocalPacketFrameSender extends EncodingPacketFrameSender implements PacketFrameSender {

    private final Consumer<ByteBuffer> receiver;

    public LocalPacketFrameSender(Consumer<ByteBuffer> receiver, int packetSizeLimit) {
        super(packetSizeLimit);
        this.receiver = receiver;
    }

    @Override
    protected int write(ByteBuffer buffer) {
        int remaining = buffer.remaining();
        receiver.accept(buffer);
        return remaining;
    }
}
