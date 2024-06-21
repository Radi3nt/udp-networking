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

import static fr.radi3nt.udp.main.Constants.sleep;

public class UdpServer {

    public void start(ConnectionFactory factory) {
        ConsumerSubscription consumerSubscription = new ConsumerSubscription();

        Map<Long, FragmentAssembler> assemblerMap = new HashMap<>();

        final FragmentAssembler assembler = new FragmentAssembler((from, buffer, termId, termOffset) -> {
            long elapsedTime = System.currentTimeMillis()-buffer.getLong();
            long elapsedTimeFromResend = System.currentTimeMillis()-buffer.getLong();
            long elapsedTimeFromSend = elapsedTime-elapsedTimeFromResend;
            System.out.println("rtt: " + elapsedTime + " ms | sent time: " + elapsedTimeFromSend + " | resend time: " + elapsedTimeFromResend);
        });

        assemblerMap.put(0L, assembler);

        FilteringFrameTypeSubscription handler = new FilteringFrameTypeSubscription(StreamSubscription.fromArray(new FragmentAssemblerSubscription(assembler)), consumerSubscription);

        Vector<FragmentingPacketStream> streamMap = new Vector<>();

        UdpConnection connection = factory.build(handler);


        FragmentingPacketStream fragmentingPacketStream = new FragmentingPacketStream(new PacketFrameSenderStream(connection.getFragmentProcessor()), 460);
        PacketStream stream = new IdentifiedPacketStream(0, new ReliablePacketStream(fragmentingPacketStream));

        streamMap.add(fragmentingPacketStream);

        NakReliabilityService reliabilityService = new NakReliabilityService(connection, assemblerMap, streamMap, UdpConnection.UDP_PACKET_SIZE, Constants.ACTIVE_TIMEOUT, Constants.INACTIVE_TIMEOUT);
        connection.setReliabilityService(consumerSubscription.add(reliabilityService));

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    ByteBuffer buffer = ByteBuffer.allocate((int) (Long.BYTES + 1.5 * (Math.pow(1024, 2))));
                    buffer.putLong(System.currentTimeMillis());
                    buffer.flip();

                    stream.packet(buffer.array());
                    sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
