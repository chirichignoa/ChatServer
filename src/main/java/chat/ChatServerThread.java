package chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.MessagesCodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Clase que representa la funcionalidad de un thread que atiende las peticiones de un cliente de chat
 * hacia el servidor propio.
 * @author Agustin Chirichigno
 * @author Braian Varona
 * @version 1.0
 */
public class ChatServerThread extends Thread implements Observer {

    private static Logger log = LogManager.getLogger(ChatServerThread.class);

    private static ChatServer chatServer;
    private Socket socket;
    private ChatMessages globalMessages; // global
    private ConcurrentHashMap<String, ChatMessages> privateMessages;
    private String userName;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    /**
     * Constructor de la clase.
     * @param chatServer instancia del servidor.
     * @param socket socket que contiene la conexion entre el servidor y el cliente.
     * @param globalMessages contenedor que implementa el patron de Observer-Observable
     *                       para los mensajes del chat global {@link ChatMessages}.
     * @see ChatMessages
     */
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

    /**
     * Metodo que contiene la logica para que el thread atienda las peticiones que llegan al servidor. Consiste en decodificar
     * la peticion para tomar la accion que corresponda.
     */
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

    /**
     * Metodo que decodifica la peticion que recibe y realiza la accion correspondiente de acuerdo al tipo de la misma.
     * @param request string con la peticion que el cliente envia.
     */
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
                log.debug("Eliminando usuario");
                this.removeUser(args[1]); //args[1] username
                break;
            default:
                break;
        }
    }

    /**
     * Metodo que se ejecuta al momento de recibir el nuevo estado del objeto observado {@link ChatMessages}.
     * @param o objeto observado.
     * @param arg nuevo estado del objeto.
     * @see ChatMessages
     */
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

    /**
     * Metodo que se realiza para registrar un nuevo usuario. Chequea si hay mas usuarios conectados y de ser asi
     * indica al servidor que realice un envio broadcast del nuevo usuario.
     * @param username nombre del usuario
     */
    private void registerUser(String username) {
        this.userName = username;
        if (ChatServer.getClientsConnected() > 0) {
            this.updateUserList(ChatServer.getUsersConnected());
        }
        chatServer.addUser(this.userName, this);
        chatServer.broadcastUser(username);
    }

    /**
     * Logica correspondiente al envio de un mensaje privado entre dos usuarios. Se verifica si no hubo un envio previo,
     * de ser asi crea un contenedor {@link ChatMessages} entre estos dos usuarios, de modo que sean notificados cada vez
     * que un nuevo mensaje es enviado. Finalmente cambia el estado del contenedor.
     * @param senderName nombre del usuario emisor del mensaje
     * @param receiverName nombre del usuario receptor del mensaje
     * @param message mensaje que se envia
     * @see ChatMessages
     */
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
            ChatServer.getRunningThreadOf(receiverName)
                    .suscribeTo(chatMessage, senderName);
        }
        chatMessage.setSenderName(senderName);
        chatMessage.setReceiverName(receiverName);
        chatMessage.setMessage(message);
    }

    /**
     * Metodo que se ejecuta en el caso de que entre dos usuarios no haya un envio previo. Le ordena al usuario receptor
     * que observe el contenedor de mensajes entre ellos, y que guarde la asociacion entre el usuario con el cual se comunica
     * y el contenedor.
     * @param chatMessages contenedor de mensajes entre el emisor y el receptor {@link ChatMessages}.
     * @param username nombre del usuario emisor.
     * @see ChatMessages
     */
    // Chequear concurrencia aca
    private synchronized void suscribeTo(ChatMessages chatMessages, String username) {
        this.privateMessages.put(username, chatMessages);
        chatMessages.addObserver(this);
    }

    /**
     * Metodo que se ejecuta en el caso de que un usuario se desconecte del chat. De modo que todos los usuarios que han mantenido una
     * conversacion con el que se desconecta no observen mas al contenedor de mensajes y eliminen la asociacion del usuario con el contenedor.
     * @param chatMessages contenedor de mensajes entre el emisor y el receptor {@link ChatMessages}.
     * @param username nombre del usuario que se desconecto.
     * @see ChatMessages
     */
    private synchronized void unsuscribeTo(ChatMessages chatMessages, String username) {
        chatMessages.deleteObserver(this);
        this.privateMessages.remove(username);
    }

    /**
     * Metodo que comunica al cliente que el usuario se ha conectado, de modo que el cliente realice la accion correspondiente.
     * @param userName nombre del usuario conectado.
     */
    public void updateUser(String userName) {
        StringBuilder builder = new StringBuilder();
        builder.append(MessagesCodes.NEW_USER).append(MessagesCodes.SEPARATOR).append(userName);
        try {
            this.dataOut.writeUTF(builder.toString());
        } catch (IOException e) {
            log.error("Error al actualizar los usuarios:" + e.getMessage() );
        }
    }

    /**
     * Metodo que envia al cliente la lista de usuarios conectados al server.
     * @param users lista de los usuarios conectados.
     */
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

    /**
     * Metodo que realiza la logica de desconexion de un usuario al server. Consiste en dejar de observar el contenedor global y cada contenedor existente
     * entre el usuario que se desconecta y los restantes, asi como tambien indicarles a los usuarios que dejen de observar el contenedor.
     * @param userName nombre del usuario que se desconecta.
     */
    private void removeUser(String userName) {
        for (Map.Entry<String, ChatMessages> chats: this.privateMessages.entrySet()) {
            chats.getValue().deleteObserver(this);
            ChatServer.getRunningThreadOf(chats.getKey()).unsuscribeTo(chats.getValue(), userName);
        }
        this.globalMessages.deleteObserver(this);
        chatServer.removeUser(userName);
        chatServer.broadcastRemoveUser(userName);
    }

    /**
     * Metodo que notifica a los clientes que un usuario se ha desconectado de modo que realicen las acciones correspondientes.
     * @param userName nombre del usuario desconectado.
     */
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
