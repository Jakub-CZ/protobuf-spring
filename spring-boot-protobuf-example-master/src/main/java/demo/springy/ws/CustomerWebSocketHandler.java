package demo.springy.ws;

import demo.CustomerProtos;
import demo.codecs.CustomerDecoder;
import demo.codecs.CustomerEncoder;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.logging.Logger;

public class CustomerWebSocketHandler extends BinaryWebSocketHandler {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        CustomerProtos.Customer customer = new CustomerDecoder().decode(message.getPayload());
        log.info("Received customer - returning UPPERCASED: " + customer);
        synchronized (this) {
            session.sendMessage(new BinaryMessage(new CustomerEncoder().encode(
                    customer.toBuilder()
                            .setFirstName(customer.getFirstName().toUpperCase())
                            .setLastName(customer.getLastName().toUpperCase())
                            .build())));
        }
    }
}