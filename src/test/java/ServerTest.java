import chat.ChatServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.MessagesCodes;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class ServerTest {
    private static Logger log = LogManager.getLogger(ServerTest.class);

    static ChatServer server;
    static Socket socket;

    @BeforeAll
    public static void initAll() {
        server = new ChatServer(2500);
    }

    @Test
    void registerUsers() {
        Thread thread = new Thread(() -> {
            server.serve();
        });
        thread.start();
        Thread t1 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri");
                    Thread.sleep(1000);
                    log.info("Cantidad de usuarios conectados: " + ChatServer.getClientsConnected());
                    Thread.sleep(1000);
                    dataOut.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri2");
                    Thread.sleep(1000);
                    log.info("Cantidad de usuarios conectados: " + ChatServer.getClientsConnected());
                    Thread.sleep(1000);
                    dataOut.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t2.start();
        Thread t3 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri3");
                    Thread.sleep(1000);
                    log.info("Cantidad de usuarios conectados: " + ChatServer.getClientsConnected());
                    Thread.sleep(1000);
                    dataOut.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t3.start();
        Thread t4 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri4");
                    Thread.sleep(1000);
                    log.info("Cantidad de usuarios conectados: " + ChatServer.getClientsConnected());
                    Thread.sleep(1000);
                    dataOut.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t4.start();

        // Espero la finalizacion de todos los threads
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void globalChat() {
        Thread thread = new Thread(() -> {
        server.serve();
        });
        thread.start();
        Thread t1 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri");
                    Thread.sleep(1000);
                    sendMsg(dataOut, "Hola soy chiri");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri2");
                    Thread.sleep(1000);
                    sendMsg(dataOut, "Hola soy chiri2");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t2.start();
        Thread t3 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri3");
                    Thread.sleep(1000);
                    sendMsg(dataOut, "Hola soy chiri3");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t3.start();
        Thread t4 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri4");
                    Thread.sleep(1000);
                    sendMsg(dataOut, "Hola soy chiri4");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t4.start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    void privateChat() {
        Thread thread = new Thread(() -> {
            server.serve();
        });
        thread.start();
        Thread t1 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri");
                    Thread.sleep(1000);
                    sendMsg(dataOut, "Chiri", "Chiri2", "Hola soy chiri");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            log.debug("thread corriendo");
            try {
                try {
                    log.debug("creando socket...");
                    Socket socket = new Socket("localhost", 2500);
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    registerNewUser(dataOut, "Chiri2");
                    Thread.sleep(5000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        t2.start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void registerNewUser(DataOutputStream dataOut, String user) {
        try {
            String message = MessagesCodes.NEW_USER +
                    MessagesCodes.SEPARATOR +
                    user;
            dataOut.writeUTF(message);
        } catch (IOException e) {
            log.error("Error al registrar nuevo usuario en el data output.");
        }
    }

    private void sendMsg(DataOutputStream dataOut, String text) {
        try {
            String message = MessagesCodes.GLOBAL_MESSAGE +
                    MessagesCodes.SEPARATOR +
                    new Date().toString() + text;
            dataOut.writeUTF(message);
        } catch (IOException e) {
            log.error("Error al enviar mensaje global en el data output.");
        }
    }

    private void sendMsg(DataOutputStream dataOut, String from, String to, String text) {
        try {
            String message = MessagesCodes.PRIVATE_MESSAGE +
                    MessagesCodes.SEPARATOR +
                    from +
                    MessagesCodes.SEPARATOR +
                    to +
                    MessagesCodes.SEPARATOR +
                    text;
            log.debug("Enviando mensaje privado: " + message);
           dataOut.writeUTF(message);
        } catch (IOException e) {
            log.error("Error al enviar mensaje privado en el data output.");
        }
    }
}


