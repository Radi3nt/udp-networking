package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.nio.ByteBuffer;
import java.util.*;

public class FragmentAssemblingUnit {

    private final Map<Long, PacketTerm> terms = new HashMap<>();
    private final Map<Long, MissingFragments> allMissed = new HashMap<>();
    private long currentTerm;
    private boolean first = true;

    public FragmentAssemblingUnit() {

    }

    public Collection<MissingFragments> getMissingParts() {
        Collection<MissingFragments> missingFragments = new ArrayList<>(allMissed.values());
        for (PacketTerm value : terms.values()) {
            if (value.termId!=currentTerm) {
                int[] missingFragmentsArray = value.missingFragmentArray();
                int lastReceivedOffset = value.lastReceivedTermOffset();
                missingFragments.add(new MissingFragments(missingFragmentsArray, lastReceivedOffset));
            }
        }
        return missingFragments;
    }

    public PacketTerm provide(ByteBuffer message, long termId, int termOffset) {
        PacketTerm packetTerm;

        if (first) {
            packetTerm = terms.computeIfAbsent(termId, PacketTerm::new);
            first = false;
        } else {
            boolean advancingFrame = currentTerm<termId;
            if (advancingFrame) {
                addMissingFrames(termId);
                currentTerm = termId;
                packetTerm = terms.computeIfAbsent(termId, PacketTerm::new);
            } else {
                packetTerm = getOldFrameIfWasMissing(termId);
                if (packetTerm == null) return null;
            }
        }

        packetTerm.add(termOffset, message);

        if (!packetTerm.isCompleted())
            return null;

        terms.remove(termId);

        return packetTerm;
    }

    private PacketTerm getOldFrameIfWasMissing(long termId) {
        MissingFragments removed = allMissed.remove(termId);
        return removed != null ? terms.computeIfAbsent(termId, PacketTerm::new) : terms.get(termId);
    }

    private void addMissingFrames(long termId) {
        for (long missedTerm = currentTerm+1; missedTerm < termId; missedTerm++) {
            allMissed.putIfAbsent(missedTerm, new MissingFragments(new int[0], -1));
        }
    }
}
