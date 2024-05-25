package fr.radi3nt.udp.actors.connection;

import fr.radi3nt.udp.actors.subscription.Subscription;

import java.io.IOException;
import java.net.SocketAddress;

public class RemoteConnectionFactory implements ConnectionFactory {

    private final SocketAddress sending;
    private final SocketAddress listening;

    public RemoteConnectionFactory(SocketAddress sending, SocketAddress listening) {
        this.sending = sending;
        this.listening = listening;
    }

    @Override
    public UdpConnection build(Subscription handler) {
        try {
            return UdpConnection.remote(sending, listening, handler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
