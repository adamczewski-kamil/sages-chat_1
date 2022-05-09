package pl.chat.message;

import lombok.Builder;
import pl.chat.file.FileData;

import java.io.Serializable;

@Builder
public class ChatMessage implements Serializable {

    private String sender;
    private String recipient;
    private String message;
    private Command command;
    private FileData file;

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Command getCommand() {
        return command;
    }

    public FileData getFile() {
        return file;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return sender != null ? sender.concat(": ").concat(message) : message;
    }
}
