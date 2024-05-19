package fr.radi3nt.udp.data.streams.datagram;

import fr.radi3nt.udp.data.streams.PacketStream;
import fr.radi3nt.udp.message.frame.FrameDataHeader;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DirectDatagramPacketStream implements PacketStream {

    private final DatagramChannel channel;

    public DirectDatagramPacketStream(DatagramChannel channel) {
        this.channel = channel;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        header.writeTo(stream);
        stream.write(data);
        channel.write(ByteBuffer.wrap(stream.toByteArray()));
    }
}
