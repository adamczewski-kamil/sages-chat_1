package pl.chat.message;

import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

@Log
public class ConsoleReader {

    private final BufferedReader reader;
    private final Consumer<String> onText;

    public ConsoleReader(InputStream inputStream, Consumer<String> onText) {
        this.onText = onText;
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public void read () {
        String text;
        try {
            while ((text = reader.readLine()) != null) {
                onText.accept(text);
            }
        } catch (IOException e) {
            log.info("Read message failed: " + e.getMessage());
        }
    }

}
