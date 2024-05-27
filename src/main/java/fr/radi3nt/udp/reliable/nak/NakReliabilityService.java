package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentAssembler;
import fr.radi3nt.udp.actors.subscription.fragment.MissingFragmentCollection;
import fr.radi3nt.udp.data.streams.FragmentingPacketStream;
import fr.radi3nt.udp.reliable.ReliabilityService;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Consumer;

public class NakReliabilityService implements ReliabilityService, Consumer<ByteBuffer> {

    private final UdpConnection connection;
    private final NakReceiver nakReceiver;
    private final NakSender nakSender;

    private final Map<Long, FragmentAssembler> assemblerMap;

    public NakReliabilityService(UdpConnection connection, Map<Long, FragmentAssembler> assemblerMap, Map<Long, FragmentingPacketStream> streamMap, int totalSize) {
        this.connection = connection;
        this.assemblerMap = assemblerMap;
        this.nakReceiver = new NakReceiver(streamMap, connection.getFragmentProcessor());
        this.nakSender = new NakSender(connection.getFragmentProcessor(), totalSize);
    }

    @Override
    public void update() {
        nakReceiver.resend();
        for (Map.Entry<Long, FragmentAssembler> entry : assemblerMap.entrySet()) {
            Map<UdpConnection, MissingFragmentCollection> missingFragmentsMap = entry.getValue().getMissingFragments();
            MissingFragmentCollection missingFragments = missingFragmentsMap.get(connection);
            if (missingFragments==null)
                return;
            nakSender.request(entry.getKey(), missingFragments.collection, missingFragments.currentTerm);
        }
    }


    public void receive(ByteBuffer buffer) {
        nakReceiver.receive(buffer);
    }

    @Override
    public void accept(ByteBuffer buffer) {
        receive(buffer);
    }
}
