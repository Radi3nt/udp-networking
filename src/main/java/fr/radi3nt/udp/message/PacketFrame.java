package fr.radi3nt.udp.message;

import fr.radi3nt.udp.message.frame.FrameType;

public class PacketFrame {

    public final FrameType frameType;
    public final byte[] data;

    public PacketFrame(FrameType frameType, byte[] data) {
        this.frameType = frameType;
        this.data = data;
    }

    public int size() {
        return data.length;
    }
}
