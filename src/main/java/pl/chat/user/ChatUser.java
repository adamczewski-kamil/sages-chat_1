package pl.chat.user;

import lombok.Data;
import lombok.extern.java.Log;
import pl.chat.history.ChatHistory;
import pl.chat.message.*;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
@Data
public class ChatUser implements Runnable {

    private static final String PUBLIC_CHAT_NAME = "public";

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Socket socket;
    private final ChatUsers chatUsers;
    private final MessageWriter writer;

    private String name = "";
    private String currentChat;

    public ChatUser(Socket socket, ChatUsers chatUsers) {
        this.socket = socket;
        this.chatUsers = chatUsers;
        writer = new MessageWriter(socket);
    }

    @Override
    public void run() {
        new MessageReader(socket, this::onChatMessage, () -> chatUsers.remove(this)).read();
    }

    public void send(ChatMessage chatMessage) {
        writer.write(chatMessage);
    }

    private void onChatMessage(ChatMessage chatMessage) {
        Command command = chatMessage.getCommand();

        if (command == Command.JOIN) {
            handleJoinChat(chatMessage);
        } else if (getName().isBlank()) {
            handleUnregisteredUser();
        } else if (command == Command.SEND) {
            handleSendFile(chatMessage);
        } else if (command == Command.SWITCH) {
            handleSwitchChat(chatMessage);
        } else if (command == Command.QUIT) {
            handleQuitChat();
        } else {
            handleBroadcastMessage(chatMessage);
        }
    }

    private void handleBroadcastMessage(ChatMessage chatMessage) {
        // send message to all users assigned to the same chat and save this message to the file with chat history
        chatUsers.broadcast(chatMessage, this);
        ChatHistory.saveMessage(chatMessage, this.currentChat);
    }

    private void handleUnregisteredUser() {
        // user not yet registered
        ChatMessage message = ChatMessageBuilder.info("You need to join the chat first: [enter $join]");
        this.send(message);
    }

    private void handleJoinChat(ChatMessage chatMessage) {
        // extract and set username
        String userName = chatMessage.getSender();
        if (chatUsers.isUsernameAvailable(userName)) {
            setName(userName);

            // assign user to PUBLIC chat
            setCurrentChat(PUBLIC_CHAT_NAME);

            // load chat history for given user
            ChatHistory.loadChatHistory(getCurrentChat(), getName(), this);

            // notify other chat members about new user joining
            ChatMessage message = ChatMessage.builder()
                    .message((getName().concat(" has joined the chat [".concat(getCurrentChat().concat("]")))))
                    .build();
            chatUsers.broadcast(message, this);
        } else {
            ChatMessage message = ChatMessageBuilder.info("There's already logged in user with username: " + chatMessage.getSender());
            send(message);
            closeSocket();
        }
    }

    private void handleSwitchChat(ChatMessage chatMessage) {
        String newChatName = chatMessage.getMessage().toLowerCase();
        if (newChatName.equals(getCurrentChat())) {
            log.log(Level.INFO, "You're already on the chat: " + newChatName);
        } else {
            // notify other chat members about user leaving
            ChatMessage message = ChatMessage.builder()
                    .message(getName().concat(" has left the chat [".concat(getCurrentChat().concat("]"))))
                    .build();
            chatUsers.broadcast(message, this);

            // assign user to the new chat
            setCurrentChat(newChatName);

            // load chat history for given user
            ChatHistory.loadChatHistory(getCurrentChat(), getName(), this);

            // notify other chat members about new user joining
            ChatMessage welcomeMessage = ChatMessage.builder()
                    .message((getName().concat(" has joined the chat [".concat(getCurrentChat().concat("]")))))
                    .build();
            chatUsers.broadcast(welcomeMessage, this);
        }
    }

    private void handleSendFile(ChatMessage chatMessage) {
        String recipientName = chatMessage.getFile().getRecipient();
        if (recipientName.equals(getName())) {
            log.log(Level.WARNING, "You cannot send file to yourself.");
        } else {
            ChatUser recipient = chatUsers.getUser(recipientName);
            if (recipient != null && recipient.getCurrentChat().equals(getCurrentChat())) {
                chatMessage.setCommand(Command.RECEIVE);
                chatUsers.broadcastSingle(chatMessage, recipient);
            } else {
                log.log(Level.WARNING, "Invalid recipient name.");
            }
        }
    }

    private void handleQuitChat() {
        ChatMessage message = ChatMessage.builder()
                .message(getName().concat(" has left the chat [".concat(getCurrentChat().concat("]"))))
                .build();
        chatUsers.broadcast(message, this);
        closeSocket();
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Closing socket failed: " + e.getMessage());
        }
    }
}
