package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.message.frame.FrameDataHeader;

public interface PacketStream {

    void packet(FrameDataHeader header, byte[] data) throws Exception;

}
