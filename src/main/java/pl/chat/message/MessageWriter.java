package pl.chat.message;

import pl.chat.client.IgnoreHeaderObjectOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageWriter {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private IgnoreHeaderObjectOutputStream writer;

    public MessageWriter(Socket socket) {
        try {
            writer = new IgnoreHeaderObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
           logger.log(Level.SEVERE, "Creating output stream failed: " + e.getMessage());
        }
    }

    public void write(ChatMessage chatMessage) {
        try {
            writer.writeObject(chatMessage);
            writer.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
