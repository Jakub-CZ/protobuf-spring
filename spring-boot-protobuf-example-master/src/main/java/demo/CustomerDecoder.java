package demo;

import com.google.protobuf.InvalidProtocolBufferException;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.nio.ByteBuffer;

public class CustomerDecoder implements Decoder.Binary<CustomerProtos.Customer> {
    @Override
    public CustomerProtos.Customer decode(ByteBuffer bytes) {
        try {
            return CustomerProtos.Customer.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean willDecode(ByteBuffer bytes) {
        return (bytes != null);
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
