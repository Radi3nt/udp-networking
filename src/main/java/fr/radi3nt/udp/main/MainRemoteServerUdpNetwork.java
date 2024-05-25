package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.RemoteConnectionFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainRemoteServerUdpNetwork {

    public static void main(String[] args) {

        String ip = args[0];
        int sending = Integer.parseInt(args[1]);
        int receiving = Integer.parseInt(args[2]);

        UdpServer server = new UdpServer();
        server.start(new RemoteConnectionFactory(new InetSocketAddress(ip, sending), new InetSocketAddress(receiving)));
    }

}
