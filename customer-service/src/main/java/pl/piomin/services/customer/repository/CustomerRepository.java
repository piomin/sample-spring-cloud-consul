package pl.piomin.services.customer.repository;

import pl.piomin.services.customer.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerRepository {

    private List<Customer> customers = new ArrayList<>();

    public Customer add(Customer customer) {
        customer.setId((long) (customers.size() + 1));
        customers.add(customer);
        return customer;
    }

    public Customer update(Customer customer) {
        customers.set(customer.getId().intValue() - 1, customer);
        return customer;
    }

    public Customer findById(Long id) {
        return customers.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    public void delete(Long id) {
        customers.remove(id.intValue());
    }

    public List<Customer> find(List<Long> ids) {
        return customers.stream().filter(p -> ids.contains(p.getId())).toList();
    }

}
