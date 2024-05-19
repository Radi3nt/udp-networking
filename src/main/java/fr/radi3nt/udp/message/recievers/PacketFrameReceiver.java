package fr.radi3nt.udp.message.recievers;

import fr.radi3nt.udp.message.PacketFrame;

import java.io.IOException;
import java.util.Collection;

public interface PacketFrameReceiver {

    Collection<PacketFrame> poll();
    void receiveMessages() throws IOException;

}
