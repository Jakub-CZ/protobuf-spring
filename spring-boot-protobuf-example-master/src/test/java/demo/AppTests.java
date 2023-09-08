package demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.websocket.DeploymentException;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        client.sendBinary(ByteBuffer.wrap(new byte[]{42, 66, 127}));
        ArrayList<ByteBuffer> response = client.getMessages();
        System.out.println("binary message received: " + Hex.encodeHexString(response.get(0)));
    }
}
