package demo;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    public void websocketLoaded() {
        WebSocketClient client = new StandardWebSocketClient();
        client.doHandshake(???)
        ResponseEntity<ByteBuffer> customerResponse = restTemplate.getForEntity("/websocket/legacy", ByteBuffer.class);
        System.out.println("customer retrieved: " + customerResponse.toString());
    }
}
