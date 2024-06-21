package fr.radi3nt.udp.reliable.nak;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentAssembler;
import fr.radi3nt.udp.actors.subscription.fragment.MissingFragmentCollection;
import fr.radi3nt.udp.data.streams.FragmentingPacketStream;
import fr.radi3nt.udp.reliable.ReliabilityService;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;

public class NakReliabilityService implements ReliabilityService, Consumer<ByteBuffer> {

    private final NakReceiver nakReceiver;
    private final NakSender nakSender;

    private final Map<Long, FragmentAssembler> assemblerMap;

    public NakReliabilityService(UdpConnection connection, Map<Long, FragmentAssembler> assemblerMap, Vector<FragmentingPacketStream> streamMap, int totalSize, int activeTimeout, int inactiveTimeout) {
        this.assemblerMap = assemblerMap;
        this.nakReceiver = new NakReceiver(streamMap, connection.getFragmentProcessor(), activeTimeout, inactiveTimeout);
        this.nakSender = new NakSender(connection.getFragmentProcessor(), totalSize, activeTimeout, inactiveTimeout);
    }

    @Override
    public void update() {
        nakReceiver.resend();
        for (Map.Entry<Long, FragmentAssembler> entry : assemblerMap.entrySet()) {
            MissingFragmentCollection missingFragments = entry.getValue().getMissingFragments();
            if (missingFragments==null)
                return;
            nakSender.request(entry.getKey(), missingFragments.collection, missingFragments.minTerm);
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
