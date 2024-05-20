package fr.radi3nt.udp.actors.subscription;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameType;

import java.util.Collection;
import java.util.Collections;

public class RawStreamSubscription implements Subscription {

    private final Subscription dataSubscription;

    public RawStreamSubscription(Subscription dataSubscription) {
        this.dataSubscription = dataSubscription;
    }

    @Override
    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
        for (PacketFrame frame : frames) {
            if (frame.getType() == FrameType.DATA)
                dataSubscription.handle(connection, Collections.singleton(frame));
        }
    }
}
