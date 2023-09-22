package demo.codecs;

import demo.CustomerProtos;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomerDecoder implements Decoder.BinaryStream<List<CustomerProtos.Customer>> {
    @Override
    public List<CustomerProtos.Customer> decode(InputStream is) throws IOException {
        List<CustomerProtos.Customer> customers = new ArrayList<>();
        CustomerProtos.Customer customer;
        while ((customer = CustomerProtos.Customer.parseDelimitedFrom(is)) != null) {
            customers.add(customer);
        }
        return customers;
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
