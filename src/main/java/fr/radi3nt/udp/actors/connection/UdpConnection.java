package fr.radi3nt.udp.actors.connection;

import fr.radi3nt.udp.actors.subscription.Subscription;
import fr.radi3nt.udp.message.recievers.BufferingPacketFrameReceiver;
import fr.radi3nt.udp.message.recievers.PacketFrameReceiver;
import fr.radi3nt.udp.message.senders.BufferingPacketFrameSender;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UdpConnection {

    private static final int UDP_PACKET_SIZE = 512;

    private final PacketFrameSender packetFrameSender;
    private final PacketFrameReceiver packetFrameReceiver;

    private final Subscription handler;

    private final DatagramChannel datagramChannel;

    public UdpConnection(SocketAddress sending, SocketAddress listening, Subscription handler) throws IOException {
        this.handler = handler;
        datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.bind(listening);
        datagramChannel.connect(sending);

        packetFrameSender = new BufferingPacketFrameSender(datagramChannel, UDP_PACKET_SIZE);
        packetFrameReceiver = new BufferingPacketFrameReceiver(datagramChannel, UDP_PACKET_SIZE);
    }

    public void update() throws IOException {
        packetFrameSender.sendFrames();
        packetFrameReceiver.receiveMessages();

        handler.handle(this, packetFrameReceiver.poll());
    }

    public PacketFrameSender getFragmentProcessor() {
        return packetFrameSender;
    }
}
