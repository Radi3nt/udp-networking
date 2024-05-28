package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.nio.ByteBuffer;
import java.util.*;

public class FragmentAssemblingUnit {

    private final Map<Long, PacketTerm> terms = new HashMap<>();
    private final Map<Long, IncompleteFragments> incompleteFragments = new HashMap<>();
    private long currentTerm;
    private boolean first = true;

    public FragmentAssemblingUnit() {

    }

    public Collection<IncompleteFragments> getMissingParts() {
        return Collections.unmodifiableCollection(incompleteFragments.values());
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

        if (!packetTerm.isCompleted()) {
            IncompleteFragments incompleteFragments = this.incompleteFragments.get(termId);
            if (incompleteFragments ==null) {
                this.incompleteFragments.put(termId, new IncompleteFragments(termId, packetTerm.receivedBits()));
            } else {
                incompleteFragments.receivedFragmentsBits = packetTerm.receivedBits();
                incompleteFragments.refreshed();
            }

            return null;
        }

        terms.remove(termId);
        incompleteFragments.remove(termId);

        return packetTerm;
    }

    private PacketTerm getOldFrameIfWasMissing(long termId) {
        IncompleteFragments removed = incompleteFragments.remove(termId);
        return removed != null ? terms.computeIfAbsent(termId, PacketTerm::new) : terms.get(termId);
    }

    private void addMissingFrames(long termId) {
        for (long missedTerm = currentTerm+1; missedTerm < termId; missedTerm++) {
            incompleteFragments.putIfAbsent(missedTerm, new IncompleteFragments(missedTerm, new BitSet()));
        }
    }

    public long getCurrentTerm() {
        return currentTerm;
    }
}
