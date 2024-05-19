package fr.radi3nt.udp.actors.subscription.fragment;

import fr.radi3nt.udp.actors.connection.UdpConnection;

import java.nio.ByteBuffer;

public interface FragmentHandler {

    void onFragment(UdpConnection from, ByteBuffer buffer, long termId, int termOffset);

}
