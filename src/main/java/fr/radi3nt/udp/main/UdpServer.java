package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.ConnectionFactory;
import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.ConsumerSubscription;
import fr.radi3nt.udp.actors.subscription.RawStreamSubscription;
import fr.radi3nt.udp.actors.subscription.Subscription;
import fr.radi3nt.udp.data.streams.*;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.reliable.nak.NakReliabilityService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static fr.radi3nt.udp.main.Constants.sleep;

public class UdpServer {

    public void start(ConnectionFactory factory) {
        ConsumerSubscription consumerSubscription = new ConsumerSubscription();
        RawStreamSubscription handler = new RawStreamSubscription(new Subscription() {
            @Override
            public void handle(UdpConnection connection, Collection<PacketFrame> frames) {

            }
        }, consumerSubscription);

        Map<Long, FragmentingPacketStream> streamMap = new HashMap<>();

        //UdpConnection connection = new UdpConnection(new LoosingLocalPacketFrameSender(clientReceiver, UdpConnection.UDP_PACKET_SIZE, LOSS_PERCENT), hostReceiver, handler);
        UdpConnection connection = factory.build(handler);
        connection.setReliabilityService(consumerSubscription.add(new NakReliabilityService(connection, new HashMap<>(), streamMap, UdpConnection.UDP_PACKET_SIZE)));

        FragmentingPacketStream fragmentingPacketStream = new FragmentingPacketStream(new PacketFrameSenderStream(connection.getFragmentProcessor()), 10);
        PacketStream stream = new IdentifiedPacketStream(0, new ReliablePacketStream(fragmentingPacketStream));

        streamMap.put(0L, fragmentingPacketStream);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                    byte[] data = new byte[940];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = (byte) i;
                    }
                    stream.packet(data);
                    sleep(3);
                    stream.packet(new byte[1]);
                    sleep(5);
                    stream.packet(new byte[] {1, 3, 7, 9});
                    sleep(2);
                    stream.packet(new byte[] {9, 8, 3, 3});
                    sleep(10);
                    stream.packet(new byte[] {9, 4, 3, 2});

                     */
                    Random random = new Random();
                    while (true) {
                        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                        buffer.putLong(System.currentTimeMillis());
                        buffer.flip();
                        stream.packet(buffer.array());
                        sleep(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        while (true) {
            try {
                connection.update();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
