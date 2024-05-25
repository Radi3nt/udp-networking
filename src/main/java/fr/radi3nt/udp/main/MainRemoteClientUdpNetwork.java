package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.RemoteConnectionFactory;

import java.net.InetSocketAddress;

public class MainRemoteClientUdpNetwork {

    public static void main(String[] args) {

        String ip = args[0];
        int sending = Integer.parseInt(args[1]);
        int receiving = Integer.parseInt(args[2]);

        UdpClient client = new UdpClient();
        client.start(new RemoteConnectionFactory(new InetSocketAddress(ip, sending), new InetSocketAddress(receiving)));

    }



}
