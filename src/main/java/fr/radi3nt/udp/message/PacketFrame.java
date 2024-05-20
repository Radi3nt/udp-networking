package fr.radi3nt.udp.message;

import fr.radi3nt.udp.message.frame.FrameHeader;
import fr.radi3nt.udp.message.frame.FrameType;

import java.nio.ByteBuffer;

public class PacketFrame {

    private final FrameHeader header;
    private final byte[] content;

    public PacketFrame(FrameHeader header, byte[] content) {
        this.header = header;
        this.content = content;
    }

    public void encode(ByteBuffer buffer) {
        header.encode(buffer);
        buffer.put(content);
    }

    public ByteBuffer getContent() {
        return ByteBuffer.wrap(content);
    }

    public int getTotalFrameSize() {
        return header.getTotalSize();
    }

    public FrameType getType() {
        return header.frameType;
    }
}
