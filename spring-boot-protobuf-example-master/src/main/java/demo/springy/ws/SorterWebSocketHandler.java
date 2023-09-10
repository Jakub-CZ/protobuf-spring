package demo.springy.ws;

import org.apache.commons.codec.binary.Hex;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

public class SorterWebSocketHandler extends BinaryWebSocketHandler {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        final ByteBuffer payload = message.getPayload();
        log.info("WS received: " + Hex.encodeHexString(payload));
        final byte[] array = payload.array();
        Arrays.sort(array);
        synchronized (this) {
            session.sendMessage(new BinaryMessage(array));
        }
    }
}