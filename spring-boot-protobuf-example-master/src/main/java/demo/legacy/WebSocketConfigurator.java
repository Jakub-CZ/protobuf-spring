package demo.legacy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@ConditionalOnWebApplication
@Configuration
@EnableWebSocket
public class WebSocketConfigurator {
    @Bean
    public CustomSpringConfigurator customSpringConfigurator() {
        return new CustomSpringConfigurator(); // This is just to get context
    }

    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }
}
