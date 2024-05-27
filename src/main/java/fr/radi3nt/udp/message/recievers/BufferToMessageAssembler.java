package fr.radi3nt.udp.message.recievers;

import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.PacketMessage;
import fr.radi3nt.udp.message.frame.FrameHeader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class BufferToMessageAssembler implements Consumer<ByteBuffer> {

    private final Queue<byte[]> messages = new ConcurrentLinkedQueue<>();

    @Override
    public void accept(ByteBuffer buffer) {
        byte[] content = buffer.array();
        messages.add(Arrays.copyOf(content, buffer.limit()));
    }

    public Collection<PacketFrame> poll() {
        Collection<PacketFrame> messages = new ArrayList<>();
        PacketMessage current;
        while ((current = pollSingle())!=null) {
            messages.addAll(current.frames);
        }
        return messages;
    }

    private PacketMessage pollSingle() {
        byte[] poll = messages.poll();
        if (poll == null) return null;

        ByteBuffer message = ByteBuffer.wrap(poll);

        Collection<PacketFrame> frames = new ArrayList<>();

        while (message.remaining()>0) {
            FrameHeader header = FrameHeader.from(message);
            int frameSize = header.getContentSize();
            byte[] frame = new byte[frameSize];
            message.get(frame);

            frames.add(new PacketFrame(header, frame));
        }

        return new PacketMessage(poll.length, frames);
    }
}
