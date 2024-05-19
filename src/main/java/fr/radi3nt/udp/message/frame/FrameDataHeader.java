package fr.radi3nt.udp.message.frame;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FrameDataHeader {

    private final ByteArrayOutputStream header;
    public final DataOutputStream stream;

    public FrameDataHeader() {
        this(new ByteArrayOutputStream());
    }

    private FrameDataHeader(ByteArrayOutputStream header) {
        this.header = header;
        stream = new DataOutputStream(header);
    }

    public FrameDataHeader child() throws IOException {
        ByteArrayOutputStream childHeader = new ByteArrayOutputStream();
        header.writeTo(childHeader);
        return new FrameDataHeader(childHeader);
    }

    public void writeTo(OutputStream stream) throws IOException {
        header.writeTo(stream);
    }

}
