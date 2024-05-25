package fr.radi3nt.udp.reliable;

import fr.radi3nt.udp.actors.connection.UdpConnection;

public interface ReliabilityServiceProvider {

    ReliabilityService provide(UdpConnection connection);

}
