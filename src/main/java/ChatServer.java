import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static Logger log = LogManager.getLogger(ChatServer.class);
    private static ConcurrentHashMap<String, ChatServerThread> runningThreads;

    private int port;
    private ServerSocket serverSocket;

    public ChatServer(int port) {
        this.port = port;
        runningThreads = new ConcurrentHashMap<>();
    }

    public void serve() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            Socket clientSocket;
            ChatMessages globalMessages = new ChatMessages(null, null);
            // Escucha por peticiones provenientes del cliente
            while(true) {
                clientSocket = serverSocket.accept();
                ChatServerThread chatServerThread = new ChatServerThread(this, clientSocket, globalMessages);
                chatServerThread.start();
            }
        } catch (IOException e) {
            log.error("Error creando el server: " + e.getMessage());
        }
    }

    public static ConcurrentHashMap<String, ChatServerThread> getRunningThreads() {
        return runningThreads;
    }

    public static ChatServerThread getRunningThreadOf(String username) {
        return runningThreads.get(username);
    }

    public void addUser(String userName, ChatServerThread thread) {
        runningThreads.put(userName, thread);
    }
}
