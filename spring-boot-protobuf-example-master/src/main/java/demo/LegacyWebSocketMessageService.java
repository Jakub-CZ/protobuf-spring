package demo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

@ServerEndpoint(value = "/websocket/legacy", configurator = CustomSpringConfigurator.class)
@Component
public class LegacyWebSocketMessageService {
    private final Logger log = Logger.getLogger(getClass().getName());

    @OnMessage
    public void onMessage(ByteBuffer byteBuffer, Session session) throws IOException {
        log.info("Received: " + byteBuffer);
        synchronized (this) {
            session.getBasicRemote().sendBinary(byteBuffer.duplicate());
        }
    }
}
