package pl.chat.user;

import lombok.extern.java.Log;
import pl.chat.message.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log
public class ChatUsers {

    private final List<ChatUser> chatUsers = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(ChatUser chatWorker) {
        lock.writeLock().lock();
        chatUsers.add(chatWorker);
        lock.writeLock().unlock();
    }

    public void remove(ChatUser chatWorker) {
        lock.writeLock().lock();
        chatUsers.remove(chatWorker);
        lock.writeLock().unlock();
    }

    public void broadcast(ChatMessage message, ChatUser sender) {
        lock.readLock().lock();
        chatUsers.stream()
                .filter(user -> user.getCurrentChat().equals(sender.getCurrentChat()))
                .forEach(user -> user.send(message));
        lock.readLock().unlock();
    }

    public void broadcastSingle(ChatMessage message, ChatUser recipient) {
        lock.readLock().lock();
        recipient.send(message);
        lock.readLock().unlock();
    }

    public boolean isUsernameAvailable(String userName) {
        return chatUsers.stream()
                .map(ChatUser::getName)
                .filter(name -> !name.isBlank())
                .noneMatch(name -> name.equals(userName));
    }

    public ChatUser getUser(String userName) {
        lock.readLock().lock();
        List<ChatUser> users = chatUsers.stream()
                .filter(user -> user.getName().equals(userName)).toList();
        lock.readLock().unlock();
        return users.isEmpty() ? null : users.get(0);
    }
}
