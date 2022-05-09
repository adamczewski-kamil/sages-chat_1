package pl.chat.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class IgnoreHeaderObjectOutputStream extends ObjectOutputStream {

    public IgnoreHeaderObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        reset();
    }
}
