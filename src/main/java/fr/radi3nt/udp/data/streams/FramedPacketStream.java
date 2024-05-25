package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.headers.FrameDataHeader;

public interface FramedPacketStream extends PacketStream {

    default void packet(FrameDataHeader header, byte[] data) throws Exception {
        packetFrame(header, data);
    }

    PacketFrame packetFrame(FrameDataHeader header, byte[] data) throws Exception;



}
