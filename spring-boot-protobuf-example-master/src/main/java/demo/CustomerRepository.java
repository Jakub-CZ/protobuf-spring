package demo;


public interface CustomerRepository {

  CustomerProtos.Customer findById(int id);

  CustomerProtos.CustomerList findAll();
}
