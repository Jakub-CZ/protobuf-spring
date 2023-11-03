package demo;

import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.websocket.AuthenticationException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.io.OutputStream;
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
    @Tag("TestAuth")
    public void websocketLoaded() throws DeploymentException, URISyntaxException, IOException {
        final ByteBuffer sentBuffer;
        ArrayList<ByteBuffer> response;
        try (WebSocketTestClientEndpoint client = WebSocketTestClientEndpoint.wsClientFactory("ws://127.0.0.1:7777/websocket/legacy")) {
            client.setMessageTrap(1);
            sentBuffer = ByteBuffer.wrap(new byte[]{42, 66, 127});
            client.sendBinary(sentBuffer.duplicate());
            response = client.getMessages();
            assertThat(response).size().isEqualTo(1);
            final ByteBuffer receivedBuffer = response.get(0);
            System.out.println("binary message received: " + Hex.encodeHexString(receivedBuffer.duplicate()));
            assertThat(receivedBuffer).isEqualTo(sentBuffer);
        }
    }

    @Test
    @Tag("TestAuth")
    public void websocketWithAuth() throws DeploymentException, URISyntaxException, IOException, AuthenticationException {
        final ByteBuffer sentBuffer;
        ArrayList<ByteBuffer> response;
        try (WebSocketTestClientEndpoint client = WebSocketTestClientEndpoint.wsClientFactory(
                "ws://127.0.0.1:7777/websocket/legacy",
                "jakub", "pa55w0rd"
        )) {
            client.setMessageTrap(1);
            sentBuffer = ByteBuffer.wrap(new byte[]{42, 66, 127});
            client.sendBinary(sentBuffer.duplicate());
            response = client.getMessages();
            assertThat(response).size().isEqualTo(1);
            final ByteBuffer receivedBuffer = response.get(0);
            System.out.println("binary message received: " + Hex.encodeHexString(receivedBuffer.duplicate()));
            assertThat(receivedBuffer).isEqualTo(sentBuffer);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/websocket/customers", "/springws/customers"})
    public void websocketWithProtobuf(String path) throws DeploymentException, URISyntaxException, IOException {
        WebSocketTestClientEndpoint client = WebSocketTestClientEndpoint.wsClientFactory("ws://127.0.0.1:7777" + path);
        client.setMessageTrap(2);
        OutputStream sendStream = client.getSendStream();
        CustomerProtos.Customer.newBuilder()
                .setFirstName("Jakub")
                .setLastName("Loucký")
                .setId(42)
                .build().writeDelimitedTo(sendStream);
        CustomerProtos.Customer.newBuilder()
                .setFirstName("John")
                .setLastName("Deere")
                .setId(69)
                .build().writeDelimitedTo(sendStream);
        sendStream.close();
        ArrayList<ByteBuffer> response = client.getMessages();
        assertThat(response).size().isEqualTo(2);
        final ByteBuffer customerAsBytes = response.get(0);
        System.out.println("customer received (in binary): " + Hex.encodeHexString(customerAsBytes.duplicate()));
        CustomerProtos.Customer customer = CustomerProtos.Customer.parseFrom(customerAsBytes);
        System.out.println("customer parsed: " + customer);
        assertThat(customer.getFirstName()).isEqualTo("JAKUB");
        assertThat(customer.getLastName()).isEqualTo("LOUCKÝ");

        CustomerProtos.Customer customer2 = CustomerProtos.Customer.parseFrom(response.get(1));
        assertThat(customer2.getLastName()).isEqualTo("DEERE");
        System.out.println("another customer: " + customer2);
        client.close();
    }

    @Test
    public void springyWebsocketSorter() throws DeploymentException, URISyntaxException, IOException {
        WebSocketTestClientEndpoint client = WebSocketTestClientEndpoint.wsClientFactory("ws://127.0.0.1:7777/springws/sortbytes");
        client.setMessageTrap(1);
        client.sendBinary(ByteBuffer.wrap(new byte[]{42, 0, -128}));
        ArrayList<ByteBuffer> response = client.getMessages();
        assertThat(response).size().isEqualTo(1);
        final ByteBuffer receivedBuffer = response.get(0);
        System.out.println("binary message received: " + Hex.encodeHexString(receivedBuffer.duplicate()));
        assertThat(receivedBuffer).isEqualTo(ByteBuffer.wrap(new byte[]{-128, 0, 42}));
        client.close();
    }
}
