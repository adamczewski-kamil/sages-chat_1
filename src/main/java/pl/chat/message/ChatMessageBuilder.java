package pl.chat.message;

import lombok.extern.java.Log;
import pl.chat.file.FileData;
import pl.chat.file.FileSender;

@Log
public class ChatMessageBuilder {

    public static ChatMessage build(String text, String sender) {
        if (text.startsWith("$join")) {
            return ChatMessage.builder()
                    .command(Command.JOIN)
                    .sender(sender)
                    .message(text)
                    .build();
        } else if (text.startsWith("$switch")) {
            String chatName = text.split(" ")[1];
            return ChatMessage.builder()
                    .command(Command.SWITCH)
                    .sender(sender)
                    .message(chatName)
                    .build();
        } else if (text.startsWith("$send")) {
            String[] textArray = text.split(" ");
            FileData fileToSend = FileSender.getFile(textArray[1], textArray[2]);
            return ChatMessage.builder()
                    .command(Command.SEND)
                    .sender(sender)
                    .file(fileToSend)
                    .build();
        } else if (text.startsWith("$quit")) {
            return ChatMessage.builder()
                    .command(Command.QUIT)
                    .sender(sender)
                    .message(text)
                    .build();
        } else {
            return ChatMessage.builder()
                    .command(Command.MESSAGE)
                    .sender(sender)
                    .message(text)
                    .build();
        }
    }

    public static ChatMessage info(String text) {
        return ChatMessage.builder()
                .command(Command.MESSAGE)
                .message(text)
                .build();
    }

}
