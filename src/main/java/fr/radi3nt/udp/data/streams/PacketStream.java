package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.headers.FrameDataHeader;

public interface PacketStream {

    void packet(FrameDataHeader header, byte[] data) throws Exception;
    default void packet(byte[] data) throws Exception {
        packet(new FrameDataHeader(), data);
    }

}
