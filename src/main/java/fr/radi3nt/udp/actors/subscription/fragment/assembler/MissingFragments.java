package fr.radi3nt.udp.actors.subscription.fragment.assembler;

import java.util.BitSet;

public class MissingFragments {

    private final BitSet receivedFragments;

    public MissingFragments(BitSet receivedFragments) {
        this.receivedFragments = receivedFragments;
    }



}
