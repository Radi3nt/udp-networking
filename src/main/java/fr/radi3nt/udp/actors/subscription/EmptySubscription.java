package fr.radi3nt.udp.actors.subscription;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.message.PacketFrame;

import java.util.Collection;

public class EmptySubscription implements Subscription {

    public static final Subscription INSTANCE = new EmptySubscription();

    @Override
    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {

    }
}
