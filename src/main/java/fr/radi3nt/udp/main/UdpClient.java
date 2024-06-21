package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.ConnectionFactory;
import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.ConsumerSubscription;
import fr.radi3nt.udp.actors.subscription.FilteringFrameTypeSubscription;
import fr.radi3nt.udp.actors.subscription.FragmentAssemblerSubscription;
import fr.radi3nt.udp.actors.subscription.StreamSubscription;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentAssembler;
import fr.radi3nt.udp.data.streams.*;
import fr.radi3nt.udp.reliable.nak.NakReliabilityService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

public class UdpClient {

    public void start(ConnectionFactory connectionFactory) {


        final AtomicReference<PacketStream> stream = new AtomicReference<>();
        final FragmentAssembler assembler = new FragmentAssembler((from, buffer, termId, termOffset) -> {
            long sendTime = buffer.getLong();

            ByteBuffer message = ByteBuffer.allocate((int) (Long.BYTES + 1 * (Math.pow(1024, 2))));
            message.putLong(sendTime);
            message.putLong(System.currentTimeMillis());

            try {
                stream.get().packet(message.array());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        ConsumerSubscription consumerSubscription = new ConsumerSubscription();
        FilteringFrameTypeSubscription handler = new FilteringFrameTypeSubscription(StreamSubscription.fromArray(new FragmentAssemblerSubscription(assembler)), consumerSubscription);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    float[] traffic = handler.traffic();
                    float dataTraffic = traffic[0]/1024f/1024;
                    float nakTraffic = traffic[1]/1024f/1024;
                    System.out.println(dataTraffic + " mb/s of data, " + nakTraffic + " mb/s of nak");
                    handler.resetTraffic();
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        Map<Long, FragmentAssembler> assemblerMap = new HashMap<>();
        assemblerMap.put(0L, assembler);

        Vector<FragmentingPacketStream> streamMap = new Vector<>();

        UdpConnection connection = connectionFactory.build(handler);
        FragmentingPacketStream fragmentingPacketStream = new FragmentingPacketStream(new PacketFrameSenderStream(connection.getFragmentProcessor()), 460);
        stream.set(new IdentifiedPacketStream(0, new ReliablePacketStream(fragmentingPacketStream)));

        streamMap.add(fragmentingPacketStream);

        connection.setReliabilityService(consumerSubscription.add(new NakReliabilityService(connection, assemblerMap, streamMap, UdpConnection.UDP_PACKET_SIZE, Constants.ACTIVE_TIMEOUT, Constants.INACTIVE_TIMEOUT)));

        while (true) {
            try {
                connection.update();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
