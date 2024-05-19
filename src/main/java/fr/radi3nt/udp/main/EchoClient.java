package fr.radi3nt.udp.main;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class EchoClient {
    private DatagramChannel socket;
    private SocketAddress address;

    private ByteBuffer buf;

    public EchoClient() throws IOException {
        socket = DatagramChannel.open();
        address = new InetSocketAddress("localhost", 4445);
        socket.bind(new InetSocketAddress("localhost", 4446));
        socket.connect(address);
    }

    public String sendEcho(String msg) throws IOException {
        buf = ByteBuffer.wrap(msg.getBytes());
        socket.write(buf);
        System.out.println("sent");
        buf.clear();
        socket.read(buf);
        System.out.println("received client");
        String received = new String(
          buf.array(), 0, buf.capacity());
        return received;
    }

    public void close() throws IOException {
        socket.close();
    }
}