package fr.radi3nt.udp.actors.subscription.fragment;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.fragment.assembler.FragmentAssemblingUnit;
import fr.radi3nt.udp.actors.subscription.fragment.assembler.PacketTerm;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class FragmentAssembler implements FragmentHandler {

    private final Map<UdpConnection, FragmentAssemblingUnit> assemblingUnitMap = new HashMap<>();
    private final FragmentHandler handler;

    public FragmentAssembler(FragmentHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onFragment(UdpConnection from, ByteBuffer buffer, long termId, int termOffset) {
        FragmentAssemblingUnit assemblingUnit = assemblingUnitMap.computeIfAbsent(from, connection -> new FragmentAssemblingUnit());
        PacketTerm term = assemblingUnit.provide(buffer, termId, termOffset);
        if (term!=null)
            handler.onFragment(from, term.assembledMessage(), termId, 0);
    }
}
