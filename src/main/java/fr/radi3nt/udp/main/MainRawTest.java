package fr.radi3nt.udp.main;

import java.io.IOException;

public class MainRawTest {

    public static void main(String[] args) throws IOException {
        new EchoServer().start();
        EchoClient client = new EchoClient();

        String echo = client.sendEcho("hello server");
        System.out.println("echo:" + echo);
        echo = client.sendEcho("server is working");
        System.out.println("echo:" + echo);
        client.sendEcho("end");
        client.close();
        System.out.println("closed");
    }

}
