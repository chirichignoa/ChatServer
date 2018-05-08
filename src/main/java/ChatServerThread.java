import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.MessagesCodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
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
                log.info("Cerrando conexion.");
                connected = false;
//                // Si se ha producido un error al recibir datos del cliente se cierra la conexion con el.
                 try {
                    this.dataIn.close();
                    this.dataOut.close();
                    this.socket.close();
                    chatServer.removeUser(this.userName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
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
                this.sendPrivateMessage(args[1], args[2], args[3]); //args[1] contains sender username args[2] contains receiver username args[3] contains message
                break;
            case MessagesCodes.REMOVE_USER:
                log.debug("Nuevo mensaje privado");
                this.removeUser(args[1]); //args[1] username
                break;
            default:
                break;
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
                // PRV|sender|receiver|message
                builder.append(MessagesCodes.PRIVATE_MESSAGE).append(MessagesCodes.SEPARATOR)
                        .append(senderName).append(MessagesCodes.SEPARATOR).append(receiverName)
                        .append(MessagesCodes.SEPARATOR).append(arg.toString());
            } else {
                // GBL|sender|message
                builder.append(MessagesCodes.GLOBAL_MESSAGE).append(MessagesCodes.SEPARATOR)
                        .append(senderName).append(MessagesCodes.SEPARATOR).append(arg.toString());
            }
            this.dataOut.writeUTF(builder.toString());
        } catch (IOException e) {
            log.error("Error al actualizar el mensaje al cliente:" + e.getMessage() );
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
    private synchronized void sendPrivateMessage(String senderName, String receiverName, String message) {
        ChatMessages chatMessage = this.privateMessages.get(receiverName);
        if ( chatMessage == null ) {
            chatMessage = new ChatMessages(senderName, receiverName);
            // Almaceno el observable para comunicar este usuario con el receptor
            this.privateMessages.put(receiverName, chatMessage);
            // Añado a este usuario como observer del observable
            chatMessage.addObserver(this);
            // Ordeno que el receptor se suscriba al observable y que lo almacene como
            // via de comunicacion con este usuario
            ChatServer.getRunningThreadOf(receiverName).suscribeTo(chatMessage, senderName);
        }
        chatMessage.setSenderName(senderName);
        chatMessage.setReceiverName(receiverName);
        chatMessage.setMessage(message);
    }

    // Chequear concurrencia aca
    private synchronized void suscribeTo(ChatMessages chatMessages, String username) {
        this.privateMessages.put(username, chatMessages);
        chatMessages.addObserver(this);
    }

    private synchronized void unsuscribeTo(ChatMessages chatMessages, String username) {
        chatMessages.deleteObserver(this);
        this.privateMessages.remove(username);
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

    private void updateUserList(Set<String> users) {
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

    private void removeUser(String userName) {
        for (Map.Entry<String, ChatMessages> chats: this.privateMessages.entrySet()) {
            chats.getValue().deleteObserver(this);
            ChatServer.getRunningThreadOf(chats.getKey()).unsuscribeTo(chats.getValue(), userName);
        }
        this.globalMessages.deleteObserver(this);
        chatServer.removeUser(userName);
        chatServer.broadcastRemoveUser(userName);
    }

    public void updateRemoveUser(String userName) {
        StringBuilder builder = new StringBuilder();
        builder.append(MessagesCodes.REMOVE_USER).append(MessagesCodes.SEPARATOR).append(userName);
        try {
            this.dataOut.writeUTF(builder.toString());
        } catch (IOException e) {
            log.error("Error al actualizar los usuarios:" + e.getMessage() );
        }
    }

}
