package fr.radi3nt.udp.message;

import java.nio.ByteBuffer;
import java.util.Collection;

public class PacketMessage {

    public final int totalSize;
    public final Collection<PacketFrame> frames;

    public PacketMessage(int totalSize, Collection<PacketFrame> frames) {
        this.totalSize = totalSize;
        this.frames = frames;
    }

    public void encode(ByteBuffer buffer) {
        for (PacketFrame frame : frames) {
            frame.encode(buffer);
        }
    }
}
