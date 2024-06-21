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

        packetTerm = getCurrentPacketTerm(termId);
        if (packetTerm == null) return null;

        packetTerm.add(termOffset, message);

        if (!packetTerm.isCompleted()) {
            IncompleteFragments incompleteFragments = this.incompleteFragments.get(termId);
            if (incompleteFragments == null) {
                return null;
            }

            incompleteFragments.receivedFragmentsBits = packetTerm.receivedBits();
            incompleteFragments.refreshed();

            return null;
        }

        terms.remove(termId);
        incompleteFragments.remove(termId);

        return packetTerm;
    }

    private PacketTerm getCurrentPacketTerm(long termId) {
        if (first) {
            first = false;
            return newTerm(termId);
        }

        PacketTerm packetTerm;

        boolean advancingFrame = currentTerm< termId;
        if (advancingFrame) {
            addMissingFrames(termId);
            currentTerm = termId;
            packetTerm = newTerm(termId);
        } else {
            packetTerm = getExistingFrame(termId);
        }
        return packetTerm;
    }

    private PacketTerm newTerm(long termId) {
        return terms.computeIfAbsent(termId, PacketTerm::new);
    }

    private PacketTerm getExistingFrame(long termId) {
        return terms.get(termId);
    }

    private void addMissingFrames(long termId) {
        PacketTerm wasCurrentTerm = terms.get(currentTerm);
        if (wasCurrentTerm!=null && !wasCurrentTerm.isCompleted()) {
            incompleteFragments.putIfAbsent(currentTerm, new IncompleteFragments(currentTerm, wasCurrentTerm.receivedBits()));
        }

        for (long missedTerm = currentTerm+1; missedTerm < termId; missedTerm++) {
            incompleteFragments.putIfAbsent(missedTerm, new IncompleteFragments(missedTerm, new BitSet()));
            newTerm(missedTerm);
        }
    }

    public long getMinTerm() {
        long minTerm = Long.MAX_VALUE;
        for (Long l : terms.keySet()) {
            minTerm = Math.min(l-1, minTerm);
        }
        if (minTerm==Long.MAX_VALUE)
            return -1;
        return minTerm;
    }
}
