package fr.radi3nt.udp.actors.subscription;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.message.PacketFrame;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class ConsumerSubscription implements Subscription {

    private final Collection<Consumer<ByteBuffer>> listeners = new ArrayList<>();

    public <T extends Consumer<ByteBuffer>> T add(T listener) {
        listeners.add(listener);
        return listener;
    }

    public void receive(ByteBuffer buffer) {
        for (Consumer<ByteBuffer> listener : listeners) {
            listener.accept(buffer);
        }
    }

    @Override
    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
        for (PacketFrame frame : frames) {
            receive(frame.getContent());
        }
    }
}
