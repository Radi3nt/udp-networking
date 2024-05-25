package fr.radi3nt.udp.headers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static fr.radi3nt.udp.data.streams.FragmentingPacketStream.LAST_MESSAGE_HINT;

public class FrameDataHeader {

    public long streamId;
    public long termId;
    public int termOffset = LAST_MESSAGE_HINT;

    public FrameDataHeader(long streamId, long termId, int termOffset) {
        this.streamId = streamId;
        this.termId = termId;
        this.termOffset = termOffset;
    }

    public FrameDataHeader() {
    }

    public FrameDataHeader child() {
        return new FrameDataHeader(streamId, termId, termOffset);
    }

    public void writeTo(ByteArrayOutputStream frameContent) throws IOException {
        DataOutputStream out = new DataOutputStream(frameContent);
        out.writeLong(streamId);
        out.writeLong(termId);
        out.writeInt(termOffset);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FrameDataHeader)) return false;

        FrameDataHeader that = (FrameDataHeader) o;
        return streamId == that.streamId && termId == that.termId && termOffset == that.termOffset;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(streamId);
        result = 31 * result + Long.hashCode(termId);
        result = 31 * result + termOffset;
        return result;
    }
}
