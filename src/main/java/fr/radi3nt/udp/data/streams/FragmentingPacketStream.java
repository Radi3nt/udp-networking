package fr.radi3nt.udp.data.streams;

import fr.radi3nt.udp.message.frame.FrameDataHeader;

import java.io.ByteArrayOutputStream;

public class FragmentingPacketStream implements PacketStream {

    public static final int LAST_MESSAGE_HINT = 1<<(8*3+7);
    private final PacketStream underlying;
    private final int maxSize;

    public FragmentingPacketStream(PacketStream underlying, int maxSize) {
        this.underlying = underlying;
        this.maxSize = maxSize;
    }

    @Override
    public void packet(FrameDataHeader header, byte[] data) throws Exception {
        int split = data.length/maxSize;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int fragmentPart = 0; fragmentPart <= split; fragmentPart++) {
            FrameDataHeader child = header.child();
            int encodingMessage = fragmentPart | (fragmentPart==split ? LAST_MESSAGE_HINT : 0);
            child.stream.writeInt(encodingMessage);

            outputStream.reset();
            writeFragmentToStream(data, outputStream, fragmentPart);

            underlying.packet(child, outputStream.toByteArray());
        }
    }

    private void writeFragmentToStream(byte[] data, ByteArrayOutputStream outputStream, int fragmentPart) {
        outputStream.write(data, fragmentPart *maxSize, Math.min(maxSize, data.length- fragmentPart *maxSize));
    }
}
