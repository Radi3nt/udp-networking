package fr.radi3nt.udp.message.senders.remote;

import fr.radi3nt.udp.message.senders.EncodingPacketFrameSender;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramPacketFrameSender extends EncodingPacketFrameSender implements PacketFrameSender {

    private final DatagramChannel channel;

    public DatagramPacketFrameSender(DatagramChannel channel, int packetSizeLimit) {
        super(packetSizeLimit);
        this.channel = channel;
    }

    @Override
    protected int write(ByteBuffer buffer) throws IOException {
        return channel.write(buffer);
    }

}
