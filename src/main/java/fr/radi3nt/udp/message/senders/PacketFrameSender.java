package fr.radi3nt.udp.message.senders;

import fr.radi3nt.udp.message.PacketFrame;

public interface PacketFrameSender {

    void addFrame(PacketFrame frame);
    void sendFrames();
}
