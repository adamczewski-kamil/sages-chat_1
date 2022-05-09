package pl.chat.message;

import lombok.extern.java.Log;
import pl.chat.client.IgnoreHeaderObjectInputStream;
import pl.chat.file.FileSender;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public class MessageReader {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Consumer<ChatMessage> onText;
    private IgnoreHeaderObjectInputStream reader;
    private Runnable onClose;

    public MessageReader(Socket socket, Consumer<ChatMessage> onText, Runnable onClose) {
        this.onText = onText;
        this.onClose = onClose;
        try {
            reader = new IgnoreHeaderObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Creating input stream failed: " + e.getMessage());
        }
    }

    public void read() {
        ChatMessage chatMessage;
        try {
            while ((chatMessage = (ChatMessage) reader.readObject()) != null) {
                if (chatMessage.getCommand() == Command.RECEIVE) {
                    log.info(chatMessage.getSender() + " sent you a file: " + chatMessage.getFile().getFileName());
                    FileSender.saveFile(chatMessage);
                } else {
                    onText.accept(chatMessage);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Read message failed: " + e.getMessage());
        } finally {
            if (onClose != null) {
                onClose.run();
            }
        }
    }

}
