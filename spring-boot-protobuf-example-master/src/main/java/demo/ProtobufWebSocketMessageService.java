package demo;

import org.springframework.stereotype.Component;

import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.logging.Logger;

@ServerEndpoint(value = "/websocket/customers", configurator = CustomSpringConfigurator.class,
        decoders = CustomerDecoder.class,
        encoders = CustomerEncoder.class)
@Component
public class ProtobufWebSocketMessageService {
    private final Logger log = Logger.getLogger(getClass().getName());

    @OnMessage
    public void onMessage(Session session, CustomerProtos.Customer customer) throws IOException, EncodeException {
        log.info("Received customer - returning UPPERCASED: " + customer);
        synchronized (this) {
            session.getBasicRemote().sendObject(customer.toBuilder()
                    .setFirstName(customer.getFirstName().toUpperCase())
                    .setLastName(customer.getLastName().toUpperCase())
                    .build()
            );
        }
    }
}
