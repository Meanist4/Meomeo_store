package service.impl;

import entity.Customer;
import java.util.List;
import repository.CustomerRepository;
import service.CustomerService;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo = new CustomerRepository();

    @Override
    public List<Customer> getAllCustomers() {
        return repo.getAll();
    }

    @Override
    public List<Customer> searchCustomers(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return repo.getAll();
        }

        return repo.search(keyword.trim());
    }

    @Override
    public boolean addCustomer(Customer customer) {

        if (repo.isPhoneExists(customer.getPhone())) {
            return false;
        }

        return repo.insert(customer);
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        return repo.update(customer);
    }

    @Override
    public boolean deleteCustomer(int id) {
        return repo.delete(id);
    }

    @Override
    public Customer getCustomerById(int id) {
        return repo.findById(id);
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return repo.isPhoneExists(phone);
    }

}