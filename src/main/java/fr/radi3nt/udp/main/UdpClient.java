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
import java.util.concurrent.atomic.AtomicReference;

public class UdpClient {

    public void start(ConnectionFactory connectionFactory) {
        Map<Long, FragmentAssembler> assemblerMap = new HashMap<>();

        final AtomicReference<PacketStream> stream = new AtomicReference<>();
        final FragmentAssembler assembler = new FragmentAssembler((from, buffer, termId, termOffset) -> {
            long sendTime = buffer.getLong();

            ByteBuffer message = ByteBuffer.allocate((int) (Long.BYTES + 1 * (Math.pow(1024, 2))));
            message.putLong(sendTime);

            try {
                stream.get().packet(message.array());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assemblerMap.put(0L, assembler);

        ConsumerSubscription consumerSubscription = new ConsumerSubscription();
        RawStreamSubscription handler = new RawStreamSubscription(new FragmentAssemblerSubscription(assembler), consumerSubscription);

        //UdpConnection connection = new UdpConnection(new LoosingLocalPacketFrameSender(hostReceiver, UdpConnection.UDP_PACKET_SIZE, LOSS_PERCENT), clientReceiver, handler);

        Map<Long, FragmentingPacketStream> streamMap = new HashMap<>();

        UdpConnection connection = connectionFactory.build(handler);
        connection.setReliabilityService(consumerSubscription.add(new NakReliabilityService(connection, assemblerMap, streamMap, UdpConnection.UDP_PACKET_SIZE)));

        FragmentingPacketStream fragmentingPacketStream = new FragmentingPacketStream(new PacketFrameSenderStream(connection.getFragmentProcessor()), 460);
        stream.set(new IdentifiedPacketStream(0, new ReliablePacketStream(fragmentingPacketStream)));

        streamMap.put(0L, fragmentingPacketStream);

        while (true) {
            try {
                connection.update();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
