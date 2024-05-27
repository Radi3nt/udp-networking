package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.headers.FrameDataHeader;
import fr.radi3nt.udp.message.PacketFrame;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FragmentingPacketStream implements PacketStream {

    public static final int LAST_MESSAGE_HINT = 1<<(8*3+7);
    private final FramedPacketStream underlying;
    private final int maxSize;

    private final Map<Long, Map<Long, PacketFrame[]>> frameMap = new HashMap<>();
    private long lastCleared = -1;

    public FragmentingPacketStream(FramedPacketStream underlying, int maxSize) {
        this.underlying = underlying;
        this.maxSize = maxSize;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        int split = (int) Math.ceil((float) data.length/maxSize);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PacketFrame[] frames = new PacketFrame[split];
        for (int fragmentPart = 0; fragmentPart < split; fragmentPart++) {
            FrameDataHeader child = header.child();
            child.termOffset = fragmentPart | (fragmentPart==(split-1) ? LAST_MESSAGE_HINT : 0);

            outputStream.reset();
            writeFragmentToStream(data, outputStream, fragmentPart);

            PacketFrame frame = underlying.packetFrame(child, outputStream.toByteArray());
            frames[fragmentPart] = frame;
        }

        Map<Long, PacketFrame[]> termIdFramesMap = frameMap.computeIfAbsent(header.streamId, aLong -> new ConcurrentHashMap<>());
        termIdFramesMap.put(header.termId, frames);
    }

    public PacketFrame[] getFrames(long streamId, long termId) {
        Map<Long, PacketFrame[]> longMap = frameMap.get(streamId);
        if (longMap==null)
            return null;
        return longMap.get(termId);
    }

    public void clearHistory(long termId) {
        if (lastCleared<termId)
            frameMap.forEach((aLong, longMap) -> longMap.keySet().removeIf(entry -> entry < termId));
        lastCleared = termId;
    }

    private void writeFragmentToStream(byte[] data, ByteArrayOutputStream outputStream, int fragmentPart) {
        outputStream.write(data, fragmentPart *maxSize, Math.min(maxSize, data.length- fragmentPart *maxSize));
    }
}
