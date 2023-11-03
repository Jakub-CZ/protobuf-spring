package demo;

import demo.codecs.CustomerDecoder;
import demo.codecs.CustomerEncoder;
import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.websocket.AuthenticationException;
import org.apache.tomcat.websocket.BasicAuthenticator;
import org.apache.tomcat.websocket.Constants;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.jupiter.api.Assertions;

import javax.websocket.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@ClientEndpoint(decoders = CustomerDecoder.class, encoders = CustomerEncoder.class)
public class WebSocketTestClientEndpoint extends Endpoint implements AutoCloseable {
    private static final boolean DEBUG = true;

    private Session session;
    private int messageTrap = 0;
    private final BlockingDeque<ByteBuffer> messageTrapBuffer = new LinkedBlockingDeque<>(2);

    public WebSocketTestClientEndpoint(URI endpointURI) throws IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, endpointURI);
    }

    public WebSocketTestClientEndpoint(URI endpointURI, String username, String password) throws IOException, DeploymentException, AuthenticationException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();
        clientEndpointConfig.getUserProperties().put(Constants.AUTHORIZATION_HEADER_NAME,
                new BasicAuthenticator().getAuthorization(null, "", username, password, null));
        container.connectToServer(this, clientEndpointConfig, endpointURI);
    }

    public static WebSocketTestClientEndpoint wsClientFactory(String uri) throws URISyntaxException, IOException, DeploymentException {
        return new WebSocketTestClientEndpoint(new URI(uri));
    }

    public static WebSocketTestClientEndpoint wsClientFactory(String uri, String username, String password)
            throws URISyntaxException, IOException, DeploymentException, AuthenticationException {
        return new WebSocketTestClientEndpoint(new URI(uri), username, password);
    }

    public ArrayList<ByteBuffer> getMessages() {
        return Assertions.assertTimeoutPreemptively(
                Duration.ofSeconds(3),
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
    public void onOpen(Session userSession, EndpointConfig config) {
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

    private final Object messageQueueLock = new Object();

    @OnMessage
    public synchronized void onMessage(ByteBuffer byteBuffer) {
        if (DEBUG) {
            //binary message from the server is CVMO packet
            System.out.printf("[WS %s] onMessage received: %s%n", Integer.toHexString(System.identityHashCode(this)), Hex.encodeHexString(byteBuffer.array()));
        }
        System.out.printf("... now %d items in queue%n", messageTrapBuffer.size());
        messageTrapBuffer.add(byteBuffer);
    }

    public void close() throws IOException {
        session.close();
    }

    public void sendBinary(ByteBuffer byteBuffer) throws IOException {
        if (DEBUG) {
            System.out.printf("sending via WS %s: %s%n",
                    Integer.toHexString(System.identityHashCode(this)),
                    Hex.encodeHexString(byteBuffer.array()));
        }
        session.getBasicRemote().sendBinary(byteBuffer);
    }

    public void sendObject(Object object) throws IOException, EncodeException {
        session.getBasicRemote().sendObject(object);
    }

    public OutputStream getSendStream() throws IOException {
        return session.getBasicRemote().getSendStream();
    }

    public void setMessageTrap() {
        setMessageTrap(1);
    }

    public void setMessageTrap(int numberOfTraps) {
        messageTrap = numberOfTraps;
    }

    public ArrayList<ByteBuffer> getMessageTraps() throws InterruptedException {
        int count = messageTrap;
        ArrayList<ByteBuffer> out = new ArrayList<>(count);
        synchronized (messageQueueLock) {
            while (count-- > 0) {
                System.out.printf("fetching one message, %d remain%n", count);
                out.add(messageTrapBuffer.take());
            }
        }
        return out;
    }
}
