package chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase que representa la funcionalidad de un servidor socket para chats entre usuarios.
 * Permitiendo que los usuarios se comuniquen globalmente y privadamente.
 * @author Agustin Chirichigno
 * @author Braian Varona
 * @version 1.0
 */
public class ChatServer {

    private static Logger log = LogManager.getLogger(ChatServer.class);
    private static ConcurrentHashMap<String, ChatServerThread> runningThreads;

    private int port;
    private ServerSocket serverSocket;

    /**
     * Constructor de la clase.
     * @param port numero de puerto por el que se escucharan las peticiones.
     */
    public ChatServer(int port) {
        this.port = port;
        runningThreads = new ConcurrentHashMap<>();
    }

    /**
     * Metodo que instancia un socket server en el puerto provisto y escucha
     * por nuevas peticiones para atenderlas mediante la creacion de threads.
     */
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

    /**
     * Metodo que retorna el numero de clientes conectados al server.
     * @return numero de clientes conectados.
     */
    public static int getClientsConnected() {
        return runningThreads.size();
    }

    /**
     * Metodo que retorna el conjunto de los nombres de usuario de cada cliente conectado.
     * @return conjunto de nombres de usuario.
     */
    public static Set<String> getUsersConnected() {
        return runningThreads.keySet();
    }

    /**
     * Metodo que obtiene la instancia del thread asociado al usuario.
     * @param username nombre de usuario para el cual buscar el thread.
     * @return la instancia del thread correspondiente.
     * @see ChatServerThread
     */
    public static ChatServerThread getRunningThreadOf(String username) {
        return runningThreads.get(username);
    }

    /**
     * Logica correspondiente al registro de un nuevo usuario. Genera una nueva entrada
     * en el registro de usuarios conectados.
     * @param userName nombre del nuevo usuario.
     * @param thread thread asociado al nuevo usuario.
     */
    public void addUser(String userName, ChatServerThread thread) {
        runningThreads.put(userName, thread);
    }

    /**
     * Logica correspondiente al logout de un usuario. Elimina la entrada en el registro
     * de usuarios conectados.
     * @param userName nombre del usuario.
     */
    public void removeUser(String userName) {
        runningThreads.remove(userName);
    }

    /**
     * Envia un mensaje a cada thread que esta en ejecucion para notificar que un nuevo
     * usuario se ha conectado.
     * @param userName nombre del nuevo usuario.
     */
    public void broadcastUser(String userName) {
        for (ChatServerThread chat: runningThreads.values()) {
            chat.updateUser(userName);
        }
    }

    /**
     * Envia un mensaje a cada thread que esta en ejecucion para notificar que un
     * usuario se ha desconectado.
     * @param userName nombre del usuario.
     */
    public void broadcastRemoveUser(String userName) {
        for (ChatServerThread chat: runningThreads.values()) {
            chat.updateRemoveUser(userName);
        }
    }
}
