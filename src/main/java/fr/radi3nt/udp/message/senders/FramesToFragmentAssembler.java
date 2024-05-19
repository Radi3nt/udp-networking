package fr.radi3nt.udp.message.senders;

import fr.radi3nt.udp.message.PacketMessage;
import fr.radi3nt.udp.message.PacketFrame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Consumer;

public class FramesToFragmentAssembler {

    private final int limitPacketSize;
    private final Consumer<PacketMessage> fragmentConsumer;

    private Collection<PacketFrame> currentFragment = new ArrayList<>();
    private int fragmentSize = 0;

    public FramesToFragmentAssembler(int limitPacketSize, Consumer<PacketMessage> fragmentConsumer) {
        this.limitPacketSize = limitPacketSize;
        this.fragmentConsumer = fragmentConsumer;
    }

    public void assemble(Queue<PacketFrame> frames) {
        while (!frames.isEmpty()) {
            PacketFrame poll = frames.poll();

            if (doesntFit(poll)) {
                sendProcess();
            }

            fragmentSize+=poll.size();
            currentFragment.add(poll);
        }

        sendProcess();
    }

    private void sendProcess() {
        if (currentFragment.isEmpty())
            return;
        sendFragment();
        resetFragment();
    }

    private void resetFragment() {
        currentFragment = new ArrayList<>();
        fragmentSize = 0;
    }

    private void sendFragment() {
        fragmentConsumer.accept(new PacketMessage(fragmentSize, currentFragment));
    }


    private boolean doesntFit(PacketFrame poll) {
        return fragmentSize + poll.size() > limitPacketSize;
    }

}
