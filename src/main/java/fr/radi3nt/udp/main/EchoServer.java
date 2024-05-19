package fr.radi3nt.udp.main;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class EchoServer extends Thread {

    private DatagramChannel socket;
    private boolean running;
    private ByteBuffer buf = ByteBuffer.allocate(1024);

    public EchoServer() throws IOException {
        socket = DatagramChannel.open();
        socket.bind(new InetSocketAddress("127.0.0.1", 4445));
        socket.connect(new InetSocketAddress("127.0.0.1", 4446));
    }

    public void run() {
        running = true;

        while (running) {
            buf.clear();

            try {
                socket.read(buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            buf.flip();


            String received 
              = new String(buf.array(), 0, buf.limit());

            //System.out.println("received " + address + " " + received);
            
            if (received.equals("end")) {
                running = false;
                continue;
            }
            try {
                socket.write(buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}