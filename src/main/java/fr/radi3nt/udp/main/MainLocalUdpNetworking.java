package fr.radi3nt.udp.main;

import fr.radi3nt.udp.actors.connection.UdpConnection;
import fr.radi3nt.udp.actors.subscription.DataStreamSubscription;
import fr.radi3nt.udp.actors.subscription.RawStreamSubscription;
import fr.radi3nt.udp.actors.subscription.Subscription;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentAssembler;
import fr.radi3nt.udp.actors.subscription.fragment.FragmentHandler;
import fr.radi3nt.udp.data.streams.FragmentingPacketStream;
import fr.radi3nt.udp.data.streams.PacketStream;
import fr.radi3nt.udp.data.streams.ReliablePacketStream;
import fr.radi3nt.udp.data.streams.datagram.PacketFrameSenderStream;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameDataHeader;
import fr.radi3nt.udp.message.recievers.local.DummyFrameReceiver;
import fr.radi3nt.udp.message.recievers.local.LocalPacketFrameReceiver;
import fr.radi3nt.udp.message.senders.local.DummyFrameSender;
import fr.radi3nt.udp.message.senders.local.LoosingLocalPacketFrameSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class MainLocalUdpNetworking {

    public static void main(String[] args) throws Exception {

        LocalPacketFrameReceiver receiver = new LocalPacketFrameReceiver(UdpConnection.UDP_PACKET_SIZE);

        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                UdpConnection connection = new UdpConnection(DummyFrameSender.INSTANCE, receiver, new RawStreamSubscription(new Subscription() {

                    final FragmentAssembler assembler = new FragmentAssembler(new FragmentHandler() {
                        @Override
                        public void onFragment(UdpConnection from, ByteBuffer buffer, long termId, int termOffset) {
                            System.out.println("received: " + Arrays.toString(buffer.array()));
                        }
                    });

                    @Override
                    public void handle(UdpConnection connection, Collection<PacketFrame> frames) {
                        for (PacketFrame frame : frames) {
                            ByteBuffer content = frame.getContent();
                            long termId = content.getLong();
                            int termOffset = content.getInt();
                            assembler.onFragment(connection, content, termId, termOffset);
                        }
                        System.out.println("missing:" + assembler.getMissingFragments());
                    }
                }));

                while (true) {
                    try {
                        connection.update();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        clientThread.start();

        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UdpConnection connection = new UdpConnection(new LoosingLocalPacketFrameSender(receiver, UdpConnection.UDP_PACKET_SIZE, 0.5f), DummyFrameReceiver.INSTANCE, new DataStreamSubscription(new HashMap<>()));
        PacketStream stream = new ReliablePacketStream(new FragmentingPacketStream(new PacketFrameSenderStream(connection.getFragmentProcessor()), 10));
        byte[] data = new byte[940];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        stream.packet(new FrameDataHeader(), data);
        stream.packet(new FrameDataHeader(), new byte[1]);

        while (true) {
            connection.update();
        }
    }

}
