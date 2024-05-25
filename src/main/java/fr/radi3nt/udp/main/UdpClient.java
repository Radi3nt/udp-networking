package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.ConnectionFactory;
import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.ConsumerSubscription;
import fr.radi3nt.udp.actors.subscription.RawStreamSubscription;
import fr.radi3nt.udp.actors.subscription.Subscription;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentAssembler;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentHandler;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.reliable.nak.NakReliabilityService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UdpClient {

    public void start(ConnectionFactory connectionFactory) {
        Map<Long, FragmentAssembler> assemblerMap = new HashMap<>();

        final FragmentAssembler assembler = new FragmentAssembler(new FragmentHandler() {
            @Override
            public void onFragment(UdpConnection from, ByteBuffer buffer, long termId, int termOffset) {

                long elapsedTime = System.currentTimeMillis()-buffer.getLong();
                System.out.println("received: " + elapsedTime + " ms");
            }
        });

        assemblerMap.put(0L, assembler);

        ConsumerSubscription consumerSubscription = new ConsumerSubscription();
        RawStreamSubscription handler = new RawStreamSubscription(new Subscription() {

            @Override
            public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
                for (PacketFrame frame : frames) {
                    ByteBuffer content = frame.getContent();
                    long streamId = content.getLong();
                    long termId = content.getLong();
                    int termOffset = content.getInt();
                    assembler.onFragment(connection, content, termId, termOffset);
                }
            }
        }, consumerSubscription);

        //UdpConnection connection = new UdpConnection(new LoosingLocalPacketFrameSender(hostReceiver, UdpConnection.UDP_PACKET_SIZE, LOSS_PERCENT), clientReceiver, handler);

        UdpConnection connection = connectionFactory.build(handler);
        connection.setReliabilityService(consumerSubscription.add(new NakReliabilityService(connection, assemblerMap, new HashMap<>(), UdpConnection.UDP_PACKET_SIZE)));

        while (true) {
            try {
                connection.update();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
