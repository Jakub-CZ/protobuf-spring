package demo;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AppTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoaded() {
        ResponseEntity<CustomerProtos.Customer> customerResponse = restTemplate.getForEntity(
                "/customers/2", CustomerProtos.Customer.class);

        System.out.println("customer retrieved: " + customerResponse.toString());
        assertThat(customerResponse.getBody().getFirstName()).isEqualTo("Josh");
    }

    @Test
    public void websocketLoaded() throws DeploymentException, URISyntaxException, IOException {
        WebSocketTestClientEndpoint client = WebSocketTestClientEndpoint.wsClientFactory("ws://127.0.0.1:7777/websocket/legacy");
        client.setMessageTrap(1);
        final ByteBuffer sentBuffer = ByteBuffer.wrap(new byte[]{42, 66, 127});
        client.sendBinary(sentBuffer.duplicate());
        ArrayList<ByteBuffer> response = client.getMessages();
        assertThat(response).size().isEqualTo(1);
        final ByteBuffer receivedBuffer = response.get(0);
        System.out.println("binary message received: " + Hex.encodeHexString(receivedBuffer.duplicate()));
        assertThat(receivedBuffer).isEqualTo(sentBuffer);
    }

    @Test
    public void websocketWithProtobuf() throws DeploymentException, URISyntaxException, IOException, EncodeException {
        WebSocketTestClientEndpoint client = WebSocketTestClientEndpoint.wsClientFactory("ws://127.0.0.1:7777/websocket/customers");
        client.setMessageTrap(1);
        client.sendObject(CustomerProtos.Customer.newBuilder()
                .setFirstName("Jakub")
                .setLastName("Loucký")
                .setId(42)
                .build());
        ArrayList<ByteBuffer> response = client.getMessages();
        assertThat(response).size().isEqualTo(1);
        final ByteBuffer customerAsBytes = response.get(0);
        System.out.println("customer received (in binary): " + Hex.encodeHexString(customerAsBytes.duplicate()));
        CustomerProtos.Customer customer = CustomerProtos.Customer.parseFrom(customerAsBytes);
        System.out.println("customer parsed: " + customer);
        assertThat(customer.getFirstName()).isEqualTo("JAKUB");
        assertThat(customer.getLastName()).isEqualTo("LOUCKÝ");
    }
}
