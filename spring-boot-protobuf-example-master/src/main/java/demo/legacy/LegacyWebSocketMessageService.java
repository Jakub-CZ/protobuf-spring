package demo.legacy;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

@ServerEndpoint(value = "/websocket/legacy", configurator = CustomSpringConfigurator.class)
@Component
public class LegacyWebSocketMessageService {
    private final Logger log = Logger.getLogger(getClass().getName());


    @OnMessage
    public void onMessage(ByteBuffer byteBuffer, Session session) throws IOException {
        log.info("UserPrincipal = " + session.getUserPrincipal());
        String sBuffer = Hex.encodeHexString(byteBuffer.duplicate());
        log.info("Received: " + sBuffer);
        synchronized (this) {
            log.info("Echoing back: " + sBuffer);
            session.getBasicRemote().sendBinary(byteBuffer);
        }
    }
}
