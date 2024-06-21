package fr.radi3nt.udp.actors.subscription;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.message.PacketFrame;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StreamSubscription implements Subscription {

    private final Map<Long, Subscription> fragmentAssemblerMap;

    public StreamSubscription(Map<Long, Subscription> fragmentAssemblerMap) {
        this.fragmentAssemblerMap = fragmentAssemblerMap;
    }

    public static StreamSubscription fromArray(Subscription... subscriptions) {
        Map<Long, Subscription> fragmentAssemblerMap = new HashMap<>();
        for (int i = 0; i < subscriptions.length; i++) {
            if (subscriptions[i]==null)
                continue;
            fragmentAssemblerMap.put((long) i, subscriptions[i]);
        }
        return new StreamSubscription(fragmentAssemblerMap);
    }

    @Override
    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
        for (PacketFrame frame : frames) {
            ByteBuffer content = frame.getContent();
            long streamId = content.getLong();
            fragmentAssemblerMap.get(streamId).handle(connection, Collections.singleton(frame));
        }
    }
}
