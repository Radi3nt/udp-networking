package fr.radi3nt.udp.message.senders.local;

import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.util.Collection;

public class DummyFrameSender implements PacketFrameSender {

    public static final DummyFrameSender INSTANCE = new DummyFrameSender();

    @Override
    public void addFrame(PacketFrame frame) {

    }

    @Override
    public void addFrames(Collection<PacketFrame> frame) {

    }

    @Override
    public void addMissingFrames(Collection<PacketFrame> frame) {

    }

    @Override
    public void sendFrames() {

    }
}
