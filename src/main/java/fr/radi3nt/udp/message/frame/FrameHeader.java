package fr.radi3nt.udp.message.frame;

import java.nio.ByteBuffer;

public class FrameHeader {

    public static final int HEADER_SIZE_BYTES = Integer.BYTES+Short.BYTES;

    public final FrameType frameType;
    public final int contentSize;

    public FrameHeader(FrameType frameType, int contentSize) {
        this.frameType = frameType;
        this.contentSize = contentSize;
    }

    public static FrameHeader from(ByteBuffer buffer) {
        int contentSize = buffer.getInt();
        FrameType type = FrameType.values()[buffer.getShort()];
        return new FrameHeader(type, contentSize);
    }

    public void encode(ByteBuffer buffer) {
        buffer.putInt(contentSize);
        buffer.putShort((short) frameType.ordinal());
    }

    public int getTotalSize() {
        return contentSize+HEADER_SIZE_BYTES;
    }

    public int getContentSize() {
        return contentSize;
    }
}
