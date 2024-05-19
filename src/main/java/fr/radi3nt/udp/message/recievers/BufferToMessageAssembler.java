package fr.radi3nt.udp.message.recievers;

import fr.radi3nt.udp.message.PacketMessage;
import fr.radi3nt.udp.message.PacketFrame;
import fr.radi3nt.udp.message.frame.FrameType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class BufferToMessageAssembler implements Consumer<ByteBuffer> {

    private final Queue<byte[]> fragments = new ConcurrentLinkedQueue<>();

    @Override
    public void accept(ByteBuffer buffer) {
        byte[] content = buffer.array();
        fragments.add(Arrays.copyOf(content, buffer.limit()));
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
        byte[] poll = fragments.poll();
        if (poll == null) return null;

        ByteBuffer fragment = ByteBuffer.wrap(poll);

        Collection<PacketFrame> frames = new ArrayList<>();

        while (fragment.remaining()>0) {
            int frameSize = fragment.getInt();
            fragment.position(fragment.position() -Integer.BYTES);
            byte[] frame = new byte[frameSize];
            fragment.get(frame);

            frames.add(new PacketFrame(FrameType.DATA, frame)); //todo needs to read data from global header
        }

        return new PacketMessage(poll.length, frames);
    }
}
