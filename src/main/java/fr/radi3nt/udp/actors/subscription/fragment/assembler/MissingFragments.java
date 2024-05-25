package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.util.Arrays;

public class MissingFragments {

    public final long termId;
    public final int[] missingFragments;
    public final int lastReceivedOffset;

    public MissingFragments(long termId, int[] missingFragments, int lastReceivedOffset) {
        this.termId = termId;
        this.missingFragments = missingFragments;
        this.lastReceivedOffset = lastReceivedOffset;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MissingFragments)) return false;

        MissingFragments that = (MissingFragments) o;
        return termId == that.termId && lastReceivedOffset == that.lastReceivedOffset && Arrays.equals(missingFragments, that.missingFragments);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(termId);
        result = 31 * result + Arrays.hashCode(missingFragments);
        result = 31 * result + lastReceivedOffset;
        return result;
    }

    @Override
    public String toString() {
        return "MissingFragments{" +
                "missingFragments=" + Arrays.toString(missingFragments) +
                ", lastReceivedOffset=" + lastReceivedOffset +
                '}';
    }
}
