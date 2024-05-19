package fr.radi3nt.udp.message.recievers;

import fr.radi3nt.udp.message.PacketFrame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Collection;

public class BufferingPacketFrameReceiver implements PacketFrameReceiver {

    private final DatagramChannel channel;
    private final ByteBuffer currentBuffer;
    private final BufferToMessageAssembler fragmentConsumer;

    public BufferingPacketFrameReceiver(DatagramChannel channel, int maxDatagramSize) {
        this.channel = channel;
        currentBuffer = ByteBuffer.allocate(maxDatagramSize);
        this.fragmentConsumer = new BufferToMessageAssembler();
    }

    @Override
    public Collection<PacketFrame> poll() {
        return fragmentConsumer.poll();
    }

    @Override
    public void receiveMessages() throws IOException {
        currentBuffer.clear();
        int byteRead = channel.read(currentBuffer);
        if (byteRead == -1) {
            throw new UnsupportedOperationException("Connection closed");
        }
        if (byteRead==0)
            return;

        System.out.println("read");

        currentBuffer.flip();
        fragmentConsumer.accept(currentBuffer);
    }
}
