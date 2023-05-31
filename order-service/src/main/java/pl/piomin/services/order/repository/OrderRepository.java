package pl.piomin.services.order.repository;

import pl.piomin.services.order.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private List<Order> orders = new ArrayList<>();

    public Order add(Order order) {
        order.setId((long) (orders.size() + 1));
        orders.add(order);
        return order;
    }

    public Order update(Order order) {
        orders.set(order.getId().intValue() - 1, order);
        return order;
    }

    public Order findById(Long id) {
        return orders.stream().filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
    }

    public void delete(Long id) {
        orders.remove(id.intValue());
    }

    public List<Order> find(List<Long> ids) {
        return orders.stream().filter(p -> ids.contains(p.getId())).toList();
    }

    public int countByCustomerId(Long customerId) {
        return (int) orders.stream().filter(p -> p.getCustomerId().equals(customerId)).count();
    }
}
