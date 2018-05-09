package chat;

import java.util.Objects;
import java.util.Observable;

/**
 * Clase que representa el contenedor de mensajes entre los threads.
 * @author Agustin Chirichigno
 * @author Braian Varona
 * @version 1.0
 */
public class ChatMessages extends Observable {

    private String message;
    private String receiverName;
    private String senderName;

    /**
     * Constructor de la clase.
     * @param senderName nombre de usuario del emisor.
     * @param receiverName nombre de usuario del receptor.
     */
    public ChatMessages(String senderName, String receiverName) {
        this.senderName = senderName;
        this.receiverName = receiverName;
    }

    /**
     * Metodo para obtener el mensaje enviado.
     * @return string del mensaje enviado.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Metodo que cambia el estado del contenedor, actualiza el mensaje.
     * @param message mensaje enviado.
     */
    public void setMessage(String message) {
        this.message = message;
        this.setChanged();
        this.notifyObservers(this.message);
    }

    /**
     * Metodo para obtener el nombre del receptor.
     * @return nombre de usuario del receptor.
     */
    public String getReceiverName() {
        return receiverName;
    }

    /**
     * Metodo para setear el nombre del receptor.
     * @param receiverName nombre del usuario receptor.
     */
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    /**
     * Metodo para obtener el nombre del emisor.
     * @return nombre de usuario del emisor.
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Metodo para setear el nombre del emisor.
     * @param senderName nombre del usuario emisor.
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessages that = (ChatMessages) o;
        return Objects.equals(message, that.message) &&
                Objects.equals(receiverName, that.receiverName) &&
                Objects.equals(senderName, that.senderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, receiverName, senderName);
    }
}
