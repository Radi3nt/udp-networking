package fr.radi3nt.udp.actors.subscription;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.message.PacketFrame;

import java.util.Collection;
import java.util.Map;

public class DataStreamSubscription implements Subscription {

    private final Map<Long, Subscription> subscriptionMap;

    public DataStreamSubscription(Map<Long, Subscription> subscriptionMap) {
        this.subscriptionMap = subscriptionMap;
    }

    @Override
    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
        for (PacketFrame frame : frames) {

        }
    }
}
