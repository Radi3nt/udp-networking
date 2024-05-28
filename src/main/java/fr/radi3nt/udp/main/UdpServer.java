package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.ConnectionFactory;
import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.ConsumerSubscription;
import fr.radi3nt.udp.actors.subscription.FragmentAssemblerSubscription;
import fr.radi3nt.udp.actors.subscription.RawStreamSubscription;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentAssembler;
import fr.radi3nt.udp.data.streams.*;
import fr.radi3nt.udp.reliable.nak.NakReliabilityService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

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

        RawStreamSubscription handler = new RawStreamSubscription(new FragmentAssemblerSubscription(assembler), consumerSubscription);

        Map<Long, FragmentingPacketStream> streamMap = new HashMap<>();

        UdpConnection connection = factory.build(handler);
        NakReliabilityService reliabilityService = new NakReliabilityService(connection, assemblerMap, streamMap, UdpConnection.UDP_PACKET_SIZE, 100);
        connection.setReliabilityService(consumerSubscription.add(reliabilityService));

        FragmentingPacketStream fragmentingPacketStream = new FragmentingPacketStream(new PacketFrameSenderStream(connection.getFragmentProcessor()), 460);
        PacketStream stream = new IdentifiedPacketStream(0, new ReliablePacketStream(fragmentingPacketStream));

        streamMap.put(0L, fragmentingPacketStream);

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    byte[] message = new byte[(int) (Long.BYTES + 200 * (Math.pow(1024, 2)))];
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.putLong(System.currentTimeMillis());
                    buffer.flip();
                    buffer.get(message, 0, Long.BYTES);

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
