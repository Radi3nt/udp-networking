package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.message.frame.FrameDataHeader;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

public class CompressingPacketStream implements PacketStream {

    private final Deflater deflater = new Deflater();
    private final PacketStream underlying;

    public CompressingPacketStream(PacketStream underlying) {
        this.underlying = underlying;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        compressed.writeTo(compressed);
        underlying.packet(header, compressed.toByteArray());
    }
}
