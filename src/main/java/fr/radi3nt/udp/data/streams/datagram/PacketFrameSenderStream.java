package fr.radi3nt.udp.data.streams.datagram;

import fr.radi3nt.udp.message.frame.FrameDataHeader;
import fr.radi3nt.udp.data.streams.PacketStream;
import fr.radi3nt.udp.message.frame.FrameHeader;
import fr.radi3nt.udp.message.frame.FrameType;
import fr.radi3nt.udp.message.senders.PacketFrameSender;
import fr.radi3nt.udp.message.PacketFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PacketFrameSenderStream implements PacketStream {

    private final PacketFrameSender packetFrameSender;

    public PacketFrameSenderStream(PacketFrameSender packetFrameSender) {
        this.packetFrameSender = packetFrameSender;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws IOException {
        byte[] fullData = writeFrameContent(header, data).toByteArray();
        packetFrameSender.addFrame(new PacketFrame(new FrameHeader(FrameType.DATA, fullData.length), fullData));
    }

    private static ByteArrayOutputStream writeFrameContent(FrameDataHeader header, byte[] data) throws IOException {
        ByteArrayOutputStream frameContent = new ByteArrayOutputStream();
        header.writeTo(frameContent);
        frameContent.write(data);
        return frameContent;
    }

}
