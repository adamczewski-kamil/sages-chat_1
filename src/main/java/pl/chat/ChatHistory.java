package pl.chat;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

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

    public static void loadChatHistory(String chat, String name, ChatWorker worker) {
        Path path = Paths.get(PATH + chat);
        if (Files.exists(path)) {
            Stream<String> lines = Stream.empty();
            try {
                lines = Files.lines(Path.of(PATH + chat));
            } catch (IOException e) {
                e.printStackTrace();
            }
            broadcastHistory(lines, worker, name);
        } else {
            log.info("no history found for: " + chat);
        }
    }

    @SneakyThrows
    public static void saveMessage(String message, String chat) {
        LOCK.writeLock().lock();
        Path path = Paths.get(PATH + chat);
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, (message + "\n").getBytes(), StandardOpenOption.APPEND);
        LOCK.writeLock().unlock();
    }

    private static void broadcastHistory(Stream<String> messages, ChatWorker worker, String name) {
        messages
                .dropWhile(message -> !message.startsWith(name))
                .forEach(worker::send);
    }
}




