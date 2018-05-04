import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(2525);
        chatServer.serve();
    }
}
