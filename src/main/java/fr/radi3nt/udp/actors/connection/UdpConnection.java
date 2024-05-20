package fr.radi3nt.udp.actors.connection;

import fr.radi3nt.udp.actors.subscription.Subscription;
import fr.radi3nt.udp.message.recievers.remote.DatagramPacketFrameReceiver;
import fr.radi3nt.udp.message.recievers.PacketFrameReceiver;
import fr.radi3nt.udp.message.senders.remote.DatagramPacketFrameSender;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UdpConnection {

    public static final int UDP_PACKET_SIZE = 512;

    private final PacketFrameSender packetFrameSender;
    private final PacketFrameReceiver packetFrameReceiver;

    private final Subscription handler;

    public UdpConnection(PacketFrameSender sender, PacketFrameReceiver receiver, Subscription handler) {
        this.handler = handler;
        packetFrameSender = sender;
        packetFrameReceiver = receiver;
    }

    public static UdpConnection remote(SocketAddress sending, SocketAddress listening, Subscription handler) throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.bind(listening);
        datagramChannel.connect(sending);
        return new UdpConnection(new DatagramPacketFrameSender(datagramChannel, UDP_PACKET_SIZE), new DatagramPacketFrameReceiver(datagramChannel, UDP_PACKET_SIZE), handler);
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
