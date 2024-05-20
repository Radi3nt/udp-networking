package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.nio.ByteBuffer;
import java.util.*;

public class FragmentAssemblingUnit {

    private final Map<Long, PacketTerm> terms = new HashMap<>();
    private long currentTerm;

    public FragmentAssemblingUnit() {

    }

    public Collection<MissingFragments> getMissingParts() {
        Collection<MissingFragments> missingFragments = new ArrayList<>();
        for (PacketTerm value : terms.values()) {
            if (value.termId!=currentTerm) {
                int[] missingFragmentsArray = value.missingFragmentArray();
                missingFragments.add(new MissingFragments(missingFragmentsArray));
            }
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
