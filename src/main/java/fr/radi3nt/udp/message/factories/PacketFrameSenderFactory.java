package fr.radi3nt.udp.message.factories;

import fr.radi3nt.udp.message.senders.PacketFrameSender;

public interface PacketFrameSenderFactory {

    PacketFrameSender provide();

}
