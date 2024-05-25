package fr.radi3nt.udp.headers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FrameNAKHeader {

    private final long streamId;
    private final long lastReceivedTermId;

    public FrameNAKHeader(long streamId, long lastReceivedTerm) {
        this.streamId = streamId;
        this.lastReceivedTermId = lastReceivedTerm;
    }

    public void writeTo(ByteArrayOutputStream outputStream) throws IOException {
        DataOutputStream out = new DataOutputStream(outputStream);
        out.writeLong(streamId);
        out.writeLong(lastReceivedTermId);
    }
}
