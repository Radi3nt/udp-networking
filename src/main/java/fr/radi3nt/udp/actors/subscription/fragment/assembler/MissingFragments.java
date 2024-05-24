package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.util.Arrays;

public class MissingFragments {

    private final int[] missingFragments;
    private final int lastReceivedOffset;

    public MissingFragments(int[] missingFragments, int lastReceivedOffset) {
        this.missingFragments = missingFragments;
        this.lastReceivedOffset = lastReceivedOffset;
    }

    @Override
    public String toString() {
        return "MissingFragments{" +
                "missingFragments=" + Arrays.toString(missingFragments) +
                ", lastReceivedOffset=" + lastReceivedOffset +
                '}';
    }
}
