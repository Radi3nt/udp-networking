package fr.radi3nt.udp.message.senders.local;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class LoosingLocalPacketFrameSender extends LocalPacketFrameSender {

    private final float lostPacketPercent;

    public LoosingLocalPacketFrameSender(Consumer<ByteBuffer> receiver, int packetSizeLimit, float lostPacketPercent) {
        super(receiver, packetSizeLimit);
        this.lostPacketPercent = lostPacketPercent;
    }

    @Override
    protected int write(ByteBuffer buffer) {
        double rolled = Math.random();
        if (rolled>=lostPacketPercent)
            return super.write(buffer);
        return buffer.remaining();
    }
}
