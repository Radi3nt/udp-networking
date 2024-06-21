package fr.radi3nt.udp.actors.subscription;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameType;

import java.util.Collection;
import java.util.Collections;

public class FilteringFrameTypeSubscription implements Subscription {

    private final Subscription dataSubscription;
    private final Subscription nakSubscription;

    private float[] traffic = new float[2];

    public FilteringFrameTypeSubscription(Subscription dataSubscription, Subscription nakSubscription) {
        this.dataSubscription = dataSubscription;
        this.nakSubscription = nakSubscription;
    }

    @Override
    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
        for (PacketFrame frame : frames) {
            if (frame.getType() == FrameType.DATA) {
                traffic[0] += frame.getTotalFrameSize();
                dataSubscription.handle(connection, Collections.singleton(frame));
            }
            if (frame.getType() == FrameType.NAK) {
                traffic[1] += frame.getTotalFrameSize();
                nakSubscription.handle(connection, Collections.singleton(frame));
            }
        }
    }

    public float[] traffic() {
        return traffic;
    }

    public void resetTraffic() {
        traffic = new float[2];
    }
}
