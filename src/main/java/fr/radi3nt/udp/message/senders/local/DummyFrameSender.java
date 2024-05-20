package fr.radi3nt.udp.message.senders.local;

import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

public class DummyFrameSender implements PacketFrameSender {

    public static final DummyFrameSender INSTANCE = new DummyFrameSender();

    @Override
    public void addFrame(PacketFrame frame) {

    }

    @Override
    public void sendFrames() {

    }
}
