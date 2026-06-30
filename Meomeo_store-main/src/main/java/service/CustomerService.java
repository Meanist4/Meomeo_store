package service;

import entity.Customer;
import java.util.List;

public interface CustomerService {

    List<Customer> getAllCustomers();

    List<Customer> searchCustomers(String keyword);

    boolean addCustomer(Customer customer);

    boolean updateCustomer(Customer customer);

    boolean deleteCustomer(int id);

    Customer getCustomerById(int id);

    boolean isPhoneExists(String phone);

}