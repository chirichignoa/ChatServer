import java.util.Objects;
import java.util.Observable;

public class ChatMessages extends Observable {

    private String message;
    private String receiverName;
    private String senderName;

    public ChatMessages(String senderName, String receiverName) {
        this.senderName = senderName;
        this.receiverName = receiverName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        this.setChanged();
        this.notifyObservers(this.message);
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getSenderName() {
        return senderName;
    }

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
