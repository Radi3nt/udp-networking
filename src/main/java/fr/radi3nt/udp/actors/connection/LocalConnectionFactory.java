package fr.radi3nt.udp.actors.connection;

import fr.radi3nt.udp.actors.subscription.Subscription;
import fr.radi3nt.udp.message.recievers.PacketFrameReceiver;
import fr.radi3nt.udp.message.recievers.local.LocalPacketFrameReceiver;
import fr.radi3nt.udp.message.senders.local.LoosingLocalPacketFrameSender;

public class LocalConnectionFactory implements ConnectionFactory {

    private final LocalPacketFrameReceiver remoteReceiver;
    private final PacketFrameReceiver localReceiver;
    private final float lostPacketPercent;

    public LocalConnectionFactory(LocalPacketFrameReceiver remoteReceiver, PacketFrameReceiver localReceiver, float lostPacketPercent) {
        this.remoteReceiver = remoteReceiver;
        this.localReceiver = localReceiver;
        this.lostPacketPercent = lostPacketPercent;
    }

    @Override
    public UdpConnection build(Subscription handler) {
        return new UdpConnection(new LoosingLocalPacketFrameSender(remoteReceiver, UdpConnection.UDP_PACKET_SIZE, lostPacketPercent), localReceiver, handler);
    }
}
