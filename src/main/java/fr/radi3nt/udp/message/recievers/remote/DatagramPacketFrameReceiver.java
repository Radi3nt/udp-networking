package fr.radi3nt.udp.message.recievers.remote;

import fr.radi3nt.udp.message.recievers.BufferPacketFrameReceiver;
import fr.radi3nt.udp.message.recievers.PacketFrameReceiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramPacketFrameReceiver extends BufferPacketFrameReceiver implements PacketFrameReceiver {

    private final DatagramChannel channel;

    public DatagramPacketFrameReceiver(DatagramChannel channel, int maxDatagramSize) {
        super(maxDatagramSize);
        this.channel = channel;
    }

    @Override
    protected int read(ByteBuffer currentBuffer) throws IOException {
        return channel.read(currentBuffer);
    }
}
