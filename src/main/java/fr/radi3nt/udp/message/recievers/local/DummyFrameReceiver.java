package fr.radi3nt.udp.message.recievers.local;

import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.recievers.PacketFrameReceiver;

import java.util.Collection;
import java.util.Collections;

public class DummyFrameReceiver implements PacketFrameReceiver {

    public static final DummyFrameReceiver INSTANCE = new DummyFrameReceiver();

    @Override
    public Collection<PacketFrame> poll() {
        return Collections.emptyList();
    }

    @Override
    public void receiveMessages() {

    }
}
