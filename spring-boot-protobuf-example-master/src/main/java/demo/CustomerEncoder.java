package demo;

import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.nio.ByteBuffer;

public class CustomerEncoder implements Encoder.Binary<CustomerProtos.Customer> {
    @Override
    public ByteBuffer encode(CustomerProtos.Customer customer) {
        return customer.toByteString().asReadOnlyByteBuffer();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // initialization not needed
    }

    @Override
    public void destroy() {
        // cleanup not needed
    }
}
