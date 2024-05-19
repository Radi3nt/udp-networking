package fr.radi3nt.udp.data.streams.datagram;

import fr.radi3nt.udp.message.frame.FrameDataHeader;
import fr.radi3nt.udp.data.streams.PacketStream;
import fr.radi3nt.udp.message.frame.FrameType;
import fr.radi3nt.udp.message.senders.PacketFrameSender;
import fr.radi3nt.udp.message.PacketFrame;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FragmentProcessorPacketStream implements PacketStream {

    private final PacketFrameSender packetFrameSender;

    public FragmentProcessorPacketStream(PacketFrameSender packetFrameSender) {
        this.packetFrameSender = packetFrameSender;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        ByteArrayOutputStream frameContent = writeFrameContent(header, data);
        ByteArrayOutputStream currentFrame = writeCurrentFrame(frameContent);
        packetFrameSender.addFrame(new PacketFrame(FrameType.DATA, currentFrame.toByteArray()));
    }

    private static ByteArrayOutputStream writeCurrentFrame(ByteArrayOutputStream frameContent) throws IOException {
        ByteArrayOutputStream currentFrame = new ByteArrayOutputStream();
        DataOutputStream currentFrameDataOutput = new DataOutputStream(currentFrame);
        currentFrameDataOutput.writeInt(frameContent.size()+Integer.BYTES);
        frameContent.writeTo(currentFrame);
        return currentFrame;
    }

    private static ByteArrayOutputStream writeFrameContent(FrameDataHeader header, byte[] data) throws IOException {
        ByteArrayOutputStream frameContent = new ByteArrayOutputStream();
        header.writeTo(frameContent);
        frameContent.write(data);
        return frameContent;
    }

}
