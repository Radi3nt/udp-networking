package fr.radi3nt.udp.message.senders;

import fr.radi3nt.udp.message.PacketMessage;
import fr.radi3nt.udp.message.PacketFrame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Consumer;

public class FramesToMessageAssembler {

    private final int limitPacketSize;
    private final Consumer<PacketMessage> messageConsumer;

    private Collection<PacketFrame> currentMessageFrames = new ArrayList<>();
    private int messageSize = 0;

    public FramesToMessageAssembler(int limitPacketSize, Consumer<PacketMessage> messageConsumer) {
        this.limitPacketSize = limitPacketSize;
        this.messageConsumer = messageConsumer;
    }

    public void assemble(Queue<PacketFrame> frames) {
        while (!frames.isEmpty()) {
            PacketFrame poll = frames.poll();

            if (doesntFit(poll)) {
                sendProcess();
            }

            messageSize += poll.getTotalFrameSize();
            currentMessageFrames.add(poll);
        }

        sendProcess();
    }

    private void sendProcess() {
        if (currentMessageFrames.isEmpty())
            return;
        sendFragment();
        resetFragment();
    }

    private void resetFragment() {
        currentMessageFrames = new ArrayList<>();
        messageSize = 0;
    }

    private void sendFragment() {
        if (messageSize>limitPacketSize)
            System.out.println("What happened?");
        messageConsumer.accept(new PacketMessage(messageSize, currentMessageFrames));
    }


    private boolean doesntFit(PacketFrame poll) {
        return messageSize + poll.getTotalFrameSize() > limitPacketSize;
    }

}
