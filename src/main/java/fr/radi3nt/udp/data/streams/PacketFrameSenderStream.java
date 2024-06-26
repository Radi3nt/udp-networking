package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.headers.FrameDataHeader;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameHeader;
import fr.radi3nt.udp.message.frame.FrameType;
import fr.radi3nt.udp.message.senders.PacketFrameSender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PacketFrameSenderStream implements FramedPacketStream {

    private final PacketFrameSender packetFrameSender;

    public PacketFrameSenderStream(PacketFrameSender packetFrameSender) {
        this.packetFrameSender = packetFrameSender;
    }

    @Override
    public PacketFrame packetFrame(FrameDataHeader header, byte[] data) throws IOException {
        PacketFrame frame = buildFrame(header, data);
        sendFrame(frame);
        return frame;
    }

    @Override
    public PacketFrame buildFrame(FrameDataHeader header, byte[] data) throws IOException {
        byte[] fullData = writeFrameContent(header, data).toByteArray();
        return new PacketFrame(new FrameHeader(FrameType.DATA, fullData.length), fullData);
    }

    @Override
    public void sendFrame(PacketFrame frame) {
        packetFrameSender.addFrame(frame);
    }

    private static ByteArrayOutputStream writeFrameContent(FrameDataHeader header, byte[] data) throws IOException {
        ByteArrayOutputStream frameContent = new ByteArrayOutputStream();
        header.writeTo(frameContent);
        frameContent.write(data);
        return frameContent;
    }

}
