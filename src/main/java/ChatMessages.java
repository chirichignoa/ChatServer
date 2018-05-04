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
}
