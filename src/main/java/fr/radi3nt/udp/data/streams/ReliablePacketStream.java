package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.headers.FrameDataHeader;

public class ReliablePacketStream implements PacketStream {

    private final PacketStream underlyingChannel;
    private long termId;

    public ReliablePacketStream(PacketStream underlyingChannel) {
        this.underlyingChannel = underlyingChannel;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        header.termId = termId;
        underlyingChannel.packet(header, data);
        termId++;
    }
}
