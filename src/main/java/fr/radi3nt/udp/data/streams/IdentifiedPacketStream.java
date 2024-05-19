package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.message.frame.FrameDataHeader;

public class IdentifiedPacketStream implements PacketStream {

    private final long streamId;
    private final PacketStream underlying;

    public IdentifiedPacketStream(long streamId, PacketStream underlying) {
        this.streamId = streamId;
        this.underlying = underlying;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        header.stream.writeLong(streamId);
        underlying.packet(header, data);
    }
}
