package fr.radi3nt.udp.actors.subscription.fragment;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.MissingFragments;

import java.util.Collection;

public class MissingFragmentCollection {

    public final long currentTerm;
    public final Collection<MissingFragments> collection;

    public MissingFragmentCollection(long currentTerm, Collection<MissingFragments> collection) {
        this.currentTerm = currentTerm;
        this.collection = collection;
    }
}
