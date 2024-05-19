package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.DataStreamSubscription;
import fr.radi3nt.udp.actors.subscription.Subscription;
import fr.radi3nt.udp.data.streams.datagram.FragmentProcessorPacketStream;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameDataHeader;
import fr.radi3nt.udp.message.frame.FrameType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainUdpNetworking {

    public static void main(String[] args) throws Exception {
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Map<Long, Subscription> subscriptionMap = new HashMap<>();
                subscriptionMap.put(0L, new Subscription() {
                    @Override
                    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
                        for (PacketFrame frame : frames) {
                            System.out.println("received: " + Arrays.toString(frame.data));
                        }
                    }
                });

                InetSocketAddress sending = new InetSocketAddress("localhost", 8888);
                InetSocketAddress listening = new InetSocketAddress("localhost", 8889);
                UdpConnection connection = null;
                try {
                    connection = new UdpConnection(sending, listening, new DataStreamSubscription(subscriptionMap));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                while (true) {
                    try {
                        connection.update();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        clientThread.start();

        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        InetSocketAddress sending = new InetSocketAddress("localhost", 8889);
        InetSocketAddress listening = new InetSocketAddress("localhost", 8888);
        UdpConnection connection = new UdpConnection(sending, listening, new DataStreamSubscription(new HashMap<>()));
        FragmentProcessorPacketStream stream = new FragmentProcessorPacketStream(connection.getFragmentProcessor());
        stream.packet(new FrameDataHeader(), new byte[] {0, 12, 31, 1});

        while (true) {
            connection.update();
        }
    }

}
