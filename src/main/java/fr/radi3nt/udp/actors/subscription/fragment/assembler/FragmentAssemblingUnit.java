package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FragmentAssemblingUnit {

    private final Map<Long, PacketTerm> terms = new HashMap<>();
    private long currentTerm;

    public FragmentAssemblingUnit() {

    }

    public Collection<MissingFragments> getMissingParts() {
        Collection<MissingFragments> missingFragments = new ArrayList<>();
        for (PacketTerm value : terms.values()) {
            if (value.termId!=currentTerm)
                missingFragments.add(new MissingFragments(value.receivedFragments()));
        }
        return missingFragments;
    }

    public PacketTerm provide(ByteBuffer message, long termId, int termOffset) {
        currentTerm = Math.max(termId, currentTerm);

        PacketTerm packetTerm = terms.computeIfAbsent(termId, PacketTerm::new);
        packetTerm.add(termOffset, message);

        if (!packetTerm.isCompleted())
            return null;

        terms.remove(termId);

        return packetTerm;
    }
}
