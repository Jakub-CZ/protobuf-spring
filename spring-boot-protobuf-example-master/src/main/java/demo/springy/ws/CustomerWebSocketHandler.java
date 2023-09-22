package demo.springy.ws;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import demo.CustomerProtos;
import demo.codecs.CustomerDecoder;
import demo.codecs.CustomerEncoder;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class CustomerWebSocketHandler extends BinaryWebSocketHandler {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        List<CustomerProtos.Customer> customers = new CustomerDecoder().decode(new ByteBufferBackedInputStream(message.getPayload()));
        log.info(String.format("Received %d customers: -->", customers.size()));
        for (CustomerProtos.Customer customer : customers) {
            log.info("Received customer - returning UPPERCASED: " + customer);
            synchronized (this) {
                session.sendMessage(new BinaryMessage(new CustomerEncoder().encode(
                        customer.toBuilder()
                                .setFirstName(customer.getFirstName().toUpperCase())
                                .setLastName(customer.getLastName().toUpperCase())
                                .build())));
            }
        }
        log.info("<-- end of customers");
    }
}