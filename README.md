#Chat_Application_Projekt_1A

1. Run ChatServer class (with one argument: port).

2. Run ChatClient class (with three arguments: host (localhost by default), port and login).

3. To join the public chat, enter $join (if there's already a user with the same login, you will not be let in)

4. All messages are saved in /src/main/resources/history/channel_name files - whenever you join in a channel that you've already been in, history will be loaded and broadcast to you (starting from your very first message sent to the given channel).

5. To send a file to another user, enter $send file_name user_name -> you can send files only to users being in the same chat. You can send files located in /src/main/resources/toSend/file_name and (these files will be saved in /src/main/resources/received/file_name).

6. To create a new chat, enter $switch chat_name (if there's already someone there, you will join them, otherwise a new chat will be created). Anyone can join you by typing $switch chat_name.

7. To quit, enter $quit.