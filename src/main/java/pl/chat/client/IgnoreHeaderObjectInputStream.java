package pl.chat.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class IgnoreHeaderObjectInputStream extends ObjectInputStream {

    public IgnoreHeaderObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected void readStreamHeader() throws IOException {
    }
}