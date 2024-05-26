package fr.radi3nt.udp.actors.subscription;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentAssembler;
import fr.radi3nt.udp.message.PacketFrame;

import java.nio.ByteBuffer;
import java.util.Collection;

public class FragmentAssemblerSubscription implements Subscription {

    private final FragmentAssembler assembler;

    public FragmentAssemblerSubscription(FragmentAssembler assembler) {
        this.assembler = assembler;
    }

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
}
