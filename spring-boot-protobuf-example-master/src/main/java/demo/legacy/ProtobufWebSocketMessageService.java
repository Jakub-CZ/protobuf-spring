package demo.legacy;

import demo.CustomerProtos;
import demo.codecs.CustomerDecoder;
import demo.codecs.CustomerEncoder;
import org.springframework.stereotype.Component;

import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@ServerEndpoint(value = "/websocket/customers", configurator = CustomSpringConfigurator.class,
        decoders = CustomerDecoder.class,
        encoders = CustomerEncoder.class)
@Component
public class ProtobufWebSocketMessageService {
    private final Logger log = Logger.getLogger(getClass().getName());

    @OnMessage
    public void onMessage(Session session, List<CustomerProtos.Customer> customers) throws IOException, EncodeException {
        log.info(String.format("Received %d customers: -->", customers.size()));
        for (CustomerProtos.Customer customer : customers) {
            log.info("Received customer - returning UPPERCASED: " + customer);
            synchronized (this) {
                session.getBasicRemote().sendObject(customer.toBuilder()
                        .setFirstName(customer.getFirstName().toUpperCase())
                        .setLastName(customer.getLastName().toUpperCase())
                        .build()
                );
            }
        }
        log.info("<-- end of customers");
    }
}
