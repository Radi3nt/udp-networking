package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.LocalConnectionFactory;
import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.message.recievers.local.LocalPacketFrameReceiver;

public class MainLocalUdpNetwork {

    public static void main(String[] args) {

        LocalPacketFrameReceiver clientReceiver = new LocalPacketFrameReceiver(UdpConnection.UDP_PACKET_SIZE);
        LocalPacketFrameReceiver serverReceiver = new LocalPacketFrameReceiver(UdpConnection.UDP_PACKET_SIZE);

        float lostPacketPercent = 0.2f;

        UdpClient client = new UdpClient();
        Thread clientThread = new Thread(() -> client.start(new LocalConnectionFactory(serverReceiver, clientReceiver, lostPacketPercent)));
        clientThread.start();

        UdpServer server = new UdpServer();
        Thread serverThread = new Thread(() -> server.start(new LocalConnectionFactory(clientReceiver, serverReceiver, lostPacketPercent)));
        serverThread.start();
    }

}
