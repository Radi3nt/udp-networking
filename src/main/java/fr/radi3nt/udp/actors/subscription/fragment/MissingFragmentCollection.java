package fr.radi3nt.udp.actors.subscription.fragment;

import fr.radi3nt.udp.actors.subscription.fragment.assembler.IncompleteFragments;

import java.util.Collection;

public class MissingFragmentCollection {

    public final long currentTerm;
    public final Collection<IncompleteFragments> collection;

    public MissingFragmentCollection(long currentTerm, Collection<IncompleteFragments> collection) {
        this.currentTerm = currentTerm;
        this.collection = collection;
    }
}
