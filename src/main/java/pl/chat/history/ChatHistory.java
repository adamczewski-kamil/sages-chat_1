package pl.chat.history;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import pl.chat.message.ChatMessage;
import pl.chat.user.ChatUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

@Log
public class ChatHistory {

    private static final String PATH = "src/main/resources/history/";
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    static {
        File directory = new File(PATH);
        if (!directory.exists()) directory.mkdirs();
    }

    public static void loadChatHistory(String chat, String name, ChatUser user) {
        Path path = Paths.get(PATH + chat);
        if (Files.exists(path)) {
            Stream<String> lines = Stream.empty();
            try {
                lines = Files.lines(Path.of(PATH + chat));
            } catch (IOException e) {
                e.printStackTrace();
            }
            broadcastHistory(lines, user);
        } else {
            log.info("no history found for: " + chat);
        }
    }

    @SneakyThrows
    public static void saveMessage(ChatMessage message, String chat) {
        LOCK.writeLock().lock();
        Path path = Paths.get(PATH + chat);
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, (message.getSender().concat(":").concat(message.getMessage()) + "\n").getBytes(), StandardOpenOption.APPEND);
        LOCK.writeLock().unlock();
    }

    private static void broadcastHistory(Stream<String> messages, ChatUser user) {
        messages
                .map(ChatHistory::mapMessage)
                .dropWhile(message -> !message.getSender().equals(user.getName()))
                .forEach(user::send);
    }

    private static ChatMessage mapMessage(String messageEntry) {
        String[] message = messageEntry.split(":");
        return ChatMessage.builder()
                .sender(message[0])
                .message(message[1])
                .build();
    }
}




