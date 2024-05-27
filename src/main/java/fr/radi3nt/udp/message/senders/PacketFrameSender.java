package fr.radi3nt.udp.message.senders;

import fr.radi3nt.udp.message.PacketFrame;

import java.util.Collection;

public interface PacketFrameSender {

    void addFrame(PacketFrame frame);
    void addFrames(Collection<PacketFrame> frame);
    void sendFrames();
}
