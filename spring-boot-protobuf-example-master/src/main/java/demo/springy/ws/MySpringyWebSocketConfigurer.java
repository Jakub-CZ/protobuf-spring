package demo.springy.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class MySpringyWebSocketConfigurer implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sorterWebSocketHandler(), "/springws/sortbytes");
        registry.addHandler(customerWebSocketHandler(), "/springws/customers");
    }

    @Bean
    public WebSocketHandler sorterWebSocketHandler() {
        return new SorterWebSocketHandler();
    }

    @Bean
    public WebSocketHandler customerWebSocketHandler() {
        return new CustomerWebSocketHandler();
    }
}