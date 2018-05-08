import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
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
            final ChatMessages globalMessages = new ChatMessages(null, null);
            // Escucha por peticiones provenientes del cliente
            while(true) {
                log.info("Server escuchando en el puerto: " + this.port);
                clientSocket = serverSocket.accept();
                ChatServerThread chatServerThread = new ChatServerThread(this, clientSocket, globalMessages);
                chatServerThread.start();
                log.info("Conexion aceptada, thread id: " + chatServerThread.getId());
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

    public void removeUser(String userName) {
        runningThreads.remove(userName);
    }

    public void broadcastUser(String userName) {
        for (ChatServerThread chat: runningThreads.values()) {
            chat.updateUser(userName);
        }
    }

    public void broadcastRemoveUser(String userName) {
        for (ChatServerThread chat: runningThreads.values()) {
            chat.updateRemoveUser(userName);
        }
    }
}
