package demo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;

import javax.websocket.*;

import org.apache.commons.codec.binary.Hex;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.jupiter.api.Assertions;

@ClientEndpoint(decoders = CustomerDecoder.class, encoders = CustomerEncoder.class)
public class WebSocketTestClientEndpoint {
    private static final boolean DEBUG = false;

    private Session session;
    private int messageTrap = 0;
    private ArrayList<ByteBuffer> messageTrapBuffer = null;

    private ClientManager client;

    public WebSocketTestClientEndpoint(URI endpointURI) throws IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, endpointURI);
    }

    public static WebSocketTestClientEndpoint wsClientFactory(String uri) throws URISyntaxException, IOException, DeploymentException {
        return new WebSocketTestClientEndpoint(new URI(uri));
    }

    public ArrayList<ByteBuffer> getMessages() {
        return Assertions.assertTimeoutPreemptively(
                Duration.ofSeconds(5),
                () -> {
                    if (DEBUG) {
                        System.out.println("TRAP: waiting for message ...");
                    }
                    ArrayList<ByteBuffer> messages = getMessageTraps();
                    if (DEBUG) {
                        System.out.println("TRAP:                     ... done");
                    }
                    return messages;
                });
    }

    @OnOpen
    public void onOpen(Session userSession) {
        //save session
        this.session = userSession;
        if (DEBUG) {
            System.out.println("opening websocket "
                    + Integer.toHexString(System.identityHashCode(this)));
        }
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        if (DEBUG) {
            System.out.println("closing websocket "
                    + Integer.toHexString(System.identityHashCode(this)));
        }
    }

    @OnMessage
    public synchronized void onMessage(ByteBuffer byteBuffer) {
        //log this message
        if (DEBUG) {
            //binary message from the server is CVMO packet
            System.out.printf("%s: %s%n", Integer.toHexString(System.identityHashCode(this)), Hex.encodeHexString(byteBuffer.array()));
        }

        //check trap
        if (messageTrap > 0) {
            //decrement trap counter
            messageTrap--;
            //allocate new array
            if (messageTrapBuffer == null) {
                messageTrapBuffer = new ArrayList<>();
            }
            //set trap data
            messageTrapBuffer.add(byteBuffer);
            //notify if last
            if (messageTrap <= 0) {
                notifyAll();
            }
        } else {
            throw new IllegalStateException(String.format("WHOOSH! A message just flew by without being processed:\n%s: %s",
                    Integer.toHexString(System.identityHashCode(this)), Hex.encodeHexString(byteBuffer.array())));
        }
    }

    public void close() throws IOException {
        session.close();
    }

    public void sendBinary(ByteBuffer byteBuffer) throws IOException {
        session.getBasicRemote().sendBinary(byteBuffer);
    }

    public void sendObject(Object object) throws IOException, EncodeException {
        session.getBasicRemote().sendObject(object);
    }

    public void setMessageTrap() {
        setMessageTrap(1);
    }

    public ByteBuffer getMessageTrap() {
        return getMessageTraps().get(0);
    }

    public synchronized void setMessageTrap(int numberOfTraps) {
        messageTrap = numberOfTraps;
        messageTrapBuffer = null;
    }

    public synchronized ArrayList<ByteBuffer> getMessageTraps() {
        while (messageTrap > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new UnsupportedOperationException("Interrupted in getMessageTrap", e);
            }
        }
        return messageTrapBuffer;
    }
}
