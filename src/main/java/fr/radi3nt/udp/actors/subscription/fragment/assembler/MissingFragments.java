package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.util.Arrays;

public class MissingFragments {

    private final int[] missingFragments;

    public MissingFragments(int[] missingFragments) {
        this.missingFragments = missingFragments;
    }

    @Override
    public String toString() {
        return "MissingFragments{" +
                "missingFragments=" + Arrays.toString(missingFragments) +
                '}';
    }
}
