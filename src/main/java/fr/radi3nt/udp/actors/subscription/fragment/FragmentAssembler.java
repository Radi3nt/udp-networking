package fr.radi3nt.udp.actors.subscription.fragment;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.fragment.assembler.FragmentAssemblingUnit;
import fr.radi3nt.udp.actors.subscription.fragment.assembler.PacketTerm;

import java.nio.ByteBuffer;

public class FragmentAssembler implements FragmentHandler {

    private final FragmentAssemblingUnit assemblingUnit = new FragmentAssemblingUnit();
    private final FragmentHandler handler;

    public FragmentAssembler(FragmentHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onFragment(UdpConnection from, ByteBuffer buffer, long termId, int termOffset) {
        PacketTerm term = assemblingUnit.provide(buffer, termId, termOffset);
        if (term!=null)
            handler.onFragment(from, term.assembledMessage(), termId, 0);
    }

    public MissingFragmentCollection getMissingFragments() {
        return new MissingFragmentCollection(assemblingUnit.getMinTerm(), assemblingUnit.getMissingParts());
    }

}
