package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.message.frame.FrameDataHeader;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BufferingPacketStream implements PacketStream {

    private final Queue<PacketBuffer> buffers = new ConcurrentLinkedDeque<>();
    private final PacketStream underlying;

    public BufferingPacketStream(PacketStream underlying) {
        this.underlying = underlying;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        buffers.add(new PacketBuffer(header, data));
        underlying.packet(header, data);
    }

    public Queue<PacketBuffer> getBuffers() {
        return buffers;
    }

    public static class PacketBuffer {

        private final FrameDataHeader header;
        private final byte[] data;

        private PacketBuffer(FrameDataHeader header, byte[] data) {
            this.header = header;
            this.data = data;
        }
    }

}
