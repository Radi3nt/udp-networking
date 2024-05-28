package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.util.BitSet;
import java.util.Objects;

public class IncompleteFragments {

    public final long termId;
    public BitSet receivedFragmentsBits;

    private boolean sent;
    private long lastSent = System.currentTimeMillis();

    public IncompleteFragments(long termId, BitSet receivedFragmentsBits) {
        this.termId = termId;
        this.receivedFragmentsBits = receivedFragmentsBits;
    }

    public void refreshed() {
        sent = false;
    }

    public void sent() {
        sent = true;
        lastSent = System.currentTimeMillis();
    }

    public boolean resendingNotNeeded(int activeResendingTimeout, int inactiveResendingTimeout) {
        long timeDiff = (System.currentTimeMillis()-lastSent);
        return sent ? timeDiff <= inactiveResendingTimeout : timeDiff <= activeResendingTimeout;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncompleteFragments)) return false;

        IncompleteFragments that = (IncompleteFragments) o;
        return termId == that.termId && Objects.equals(receivedFragmentsBits, that.receivedFragmentsBits);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(termId);
        result = 31 * result + Objects.hashCode(receivedFragmentsBits);
        return result;
    }
}
