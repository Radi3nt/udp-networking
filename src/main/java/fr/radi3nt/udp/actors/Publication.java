package fr.radi3nt.udp.actors;

import java.nio.ByteBuffer;

public interface Publication {

    void offer(byte[] data);
    void offer(ByteBuffer data);

    void give(byte[] data);
    void give(ByteBuffer data);

}
