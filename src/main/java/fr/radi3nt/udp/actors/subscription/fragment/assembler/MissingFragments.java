package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.util.BitSet;
import java.util.Objects;

public class MissingFragments {

    public final long termId;
    public final BitSet receivedFragmentsBits;

    public MissingFragments(long termId, BitSet receivedFragmentsBits) {
        this.termId = termId;
        this.receivedFragmentsBits = receivedFragmentsBits;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MissingFragments)) return false;

        MissingFragments that = (MissingFragments) o;
        return termId == that.termId && Objects.equals(receivedFragmentsBits, that.receivedFragmentsBits);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(termId);
        result = 31 * result + Objects.hashCode(receivedFragmentsBits);
        return result;
    }
}
