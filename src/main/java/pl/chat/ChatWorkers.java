package pl.chat;

import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log
public class ChatWorkers {

    private final List<ChatWorker> chatWorkers = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(ChatWorker chatWorker) {
        lock.writeLock().lock();
        chatWorkers.add(chatWorker);
        lock.writeLock().unlock();
    }

    public void remove(ChatWorker chatWorker) {
        lock.writeLock().lock();
        chatWorkers.remove(chatWorker);
        lock.writeLock().unlock();
    }

    public void broadcast(String message, ChatWorker chatWorker) {
        lock.readLock().lock();
        chatWorkers.stream()
                .filter(worker -> worker.getCurrentChat().equals(chatWorker.getCurrentChat()))
                .forEach(worker -> worker.send(message));
        lock.readLock().unlock();
    }

    public boolean isUsernameAvailable(String userName) {
        return chatWorkers.stream()
                .map(ChatWorker::getName)
                .filter(name -> !name.isBlank())
                .noneMatch(name -> name.equals(userName));
    }
}
