package pl.chat;

import lombok.Data;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
@Data
public class ChatWorker implements Runnable {

    private static final String START_SESSION_COMMAND = "START";
    private static final String END_SESSION_COMMAND = "QUIT";
    private static final String SWITCH_CHAT_COMMAND = "SWITCH";
    private static final String PUBLIC_CHAT_NAME = "PUBLIC";
    private static final String SEND_FILE_COMMAND = "SEND";

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Socket socket;
    private final ChatWorkers chatWorkers;
    private final MessageWriter writer;

    private String name = "";
    private String currentChat;

    public ChatWorker(Socket socket, ChatWorkers chatWorkers) {
        this.socket = socket;
        this.chatWorkers = chatWorkers;
        writer = new MessageWriter(socket);
    }

    @Override
    public void run() {
        new MessageReader(socket, this::onText, () -> chatWorkers.remove(this)).read();
    }

    public void send(String message) {
        writer.write(message);
    }

    private void onText(String text) {
        if (text.contains(START_SESSION_COMMAND)) {
            handleStartChat(text);
        } else if (getName().isBlank()) {
            handleUnregisteredUser();
        } else if (text.contains(SWITCH_CHAT_COMMAND)) {
            handleSwitchChat(text);
        } else if (text.endsWith(END_SESSION_COMMAND)) {
            handleQuitChat();
        } else {
            handleBroadcastMessage(text);
        }
    }

    private void handleBroadcastMessage(String text) {
        // send message to all users assigned to the same chat and save this message to the file with chat history
        chatWorkers.broadcast(text, this);
        ChatHistory.saveMessage(text, this.currentChat);
    }

    private void handleUnregisteredUser() {
        // user not yet registered
        this.send("You need to join the chat first: [enter START]");
    }

    private void handleStartChat(String text) {
        // extract and set username
        int colonIndex = text.indexOf(":");
        String userName = text.substring(0, colonIndex);
        if (chatWorkers.isUsernameAvailable(userName)) {
            setName(userName);

            // assign user to PUBLIC chat
            setCurrentChat(PUBLIC_CHAT_NAME);

            // load chat history for given user
            ChatHistory.loadChatHistory(getCurrentChat(), getName(), this);

            // notify other chat members about new user joining
            chatWorkers.broadcast(getName() + " has joined the chat [" + getCurrentChat() + "] ", this);
        } else {
            send("There's already logged in user with username: " + userName);
            closeSocket();
        }
    }

    private void handleSwitchChat(String text) {
        // process the input
        String[] inputString = text.split(" ");
        String newChatName;
        if (inputString.length == 3) {
            // notify other chat members about user leaving
            chatWorkers.broadcast(getName() + " has left the chat [" + getCurrentChat() + "] ", this);

            // assign user to the new chat
            newChatName = inputString[2];
            setCurrentChat(newChatName);

            // load chat history for given user
            ChatHistory.loadChatHistory(getCurrentChat(), getName(), this);

            // notify other chat members about new user joining
            chatWorkers.broadcast(getName() + " has joined the chat [" + getCurrentChat() + "] ", this);
        } else {
            log.log(Level.INFO, "Invalid input.");
        }
    }

    private void handleQuitChat() {
        chatWorkers.broadcast(getName() + " has left the chat [" + getCurrentChat() + "] ", this);
        closeSocket();
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Closing socked failed: " + e.getMessage());
        }
    }
}
