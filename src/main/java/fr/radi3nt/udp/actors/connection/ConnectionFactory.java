package fr.radi3nt.udp.actors.connection;

import fr.radi3nt.udp.actors.subscription.Subscription;

public interface ConnectionFactory {

    UdpConnection build(Subscription handler);

}
