package fr.radi3nt.udp.actors.subscription.fragment;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.IncompleteFragments;

import java.util.Collection;

public class MissingFragmentCollection {

    public final long minTerm;
    public final Collection<IncompleteFragments> collection;

    public MissingFragmentCollection(long minTerm, Collection<IncompleteFragments> collection) {
        this.minTerm = minTerm;
        this.collection = collection;
    }
}
