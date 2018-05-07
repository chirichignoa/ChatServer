import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.MessagesCodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServerThread extends Thread implements Observer {

    private static Logger log = LogManager.getLogger(ChatServerThread.class);

    private static ChatServer chatServer;
    private Socket socket;
    private ChatMessages globalMessages; // global
    private ConcurrentHashMap<String, ChatMessages> privateMessages;
    private String userName;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    public ChatServerThread(ChatServer chatServer, Socket socket, ChatMessages globalMessages) {
        ChatServerThread.chatServer = chatServer;
        this.socket = socket;
        this.globalMessages = globalMessages;
        this.privateMessages = new ConcurrentHashMap<>();
        try {
            this.dataIn = new DataInputStream(socket.getInputStream());
            this.dataOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            log.error("Error creando los data input/output: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        String receivedMessage;
        boolean connected = true;
        this.globalMessages.addObserver(this);

        // Mientras la conexion este vigente
        while(connected) {
            try {
                receivedMessage = this.dataIn.readUTF();
                this.decodeRequest(receivedMessage);
            } catch (IOException readException) {
                // desconectar usuarios
                log.error("Error al leer mensaje, puede que se haya cerrado la conexion: " + readException.getMessage());
                connected = false;
                // Si se ha producido un error al recibir datos del cliente se cierra la conexion con el.
                try {
                    this.dataIn.close();
                    this.dataOut.close();
                    this.socket.close();
                } catch (IOException closeException) {
                    log.error("Error cerrando los data input/output:" + closeException.getMessage());
                }

            }
        }
    }

    @Override
    public synchronized void update(Observable o, Object arg) {
        // Recibo una notificacion de que el mensaje ha cambiado por lo que debo actualizarle al cliente
        try {
            // Envia el mensaje al cliente y este lo discrimina para mostrarlo en la seccion correcta
            StringBuilder builder = new StringBuilder();
            String senderName = ((ChatMessages)o).getSenderName();
            String receiverName = ((ChatMessages)o).getReceiverName();
            if( (senderName != null) && (receiverName!= null) ) {
                // PRV|sender|message
                builder.append(MessagesCodes.PRIVATE_MESSAGE);
            } else {
                // GBL|sender|message
                builder.append(MessagesCodes.GLOBAL_MESSAGE);
            }
            builder.append(MessagesCodes.SEPARATOR).
                    append(senderName).append(MessagesCodes.SEPARATOR).append(arg.toString());

            this.dataOut.writeUTF(builder.toString());
        } catch (IOException e) {
            log.error("Error al actualizar el mensaje al cliente:" + e.getMessage() );
        }
    }

    private void decodeRequest(String request) {
        String[] args = request.split("\\" + MessagesCodes.SEPARATOR);
        log.debug("New request -- " + request);
        switch (args[0]) {
            case MessagesCodes.NEW_USER:
                log.debug("Nuevo usuario: " + args[1]);
                this.registerUser(args[1]); //args[1] contains username
                break;
            case MessagesCodes.GLOBAL_MESSAGE:
                log.debug("Nuevo mensaje global");
                this.globalMessages.setSenderName(this.userName);
                this.globalMessages.setMessage(args[1]); //args[1] contains global message
                break;
            case MessagesCodes.PRIVATE_MESSAGE:
                log.debug("Nuevo mensaje privado");
                this.sendPrivateMessage(args[1], args[2]); //args[1] contains receiver username args[2] contains message
                break;
            default:
                break;
        }
    }

    private void registerUser(String username) {
        this.userName = username;
        if (ChatServer.getRunningThreads().size() > 0) {
            this.updateUserList(ChatServer.getRunningThreads().keySet());
        }
        chatServer.addUser(this.userName, this);
        chatServer.broadcastUser(username);
    }

    // Chequear concurrencia aca
    private synchronized void sendPrivateMessage(String receiverName, String message) {
        ChatMessages chatMessage = this.privateMessages.get(receiverName);
        if ( chatMessage == null ) {
            chatMessage = new ChatMessages(this.userName, receiverName);
            // Almaceno el observable para comunicar este usuario con el receptor
            this.privateMessages.put(receiverName, chatMessage);
            // Añado a este usuario como observer del observable
            chatMessage.addObserver(this);
            // Ordeno que el receptor se suscriba al observable y que lo almacene como
            // via de comunicacion con este usuario
            ChatServer.getRunningThreadOf(receiverName).suscribeTo(chatMessage, this.userName);
        } else {
            chatMessage.setSenderName(this.userName);
            chatMessage.setReceiverName(receiverName);
        }
        chatMessage.setMessage(message);
    }

    // Chequear concurrencia aca
    public synchronized void suscribeTo(ChatMessages chatMessages, String username) {
        this.privateMessages.put(username, chatMessages);
        chatMessages.addObserver(this);
    }

    public void updateUser(String userName) {
        StringBuilder builder = new StringBuilder();
        builder.append(MessagesCodes.NEW_USER).append(MessagesCodes.SEPARATOR).append(userName);
        try {
            this.dataOut.writeUTF(builder.toString());
        } catch (IOException e) {
            log.error("Error al actualizar los usuarios:" + e.getMessage() );
        }
    }

    public void updateUserList(Set<String> users) {
        StringBuilder builder = new StringBuilder();
        builder.append(MessagesCodes.GET_USERS);
        for (String user :
                users) {
            builder.append(MessagesCodes.SEPARATOR).append(user);
        }
        try {
            this.dataOut.writeUTF(builder.toString());
        } catch (IOException e) {
            log.error("Error al actualizar la lista de usuarios:" + e.getMessage() );
        }
    }
}
