package hr.fer.ecommerce.order.repository;

import hr.fer.ecommerce.order.model.Order;
import hr.fer.ecommerce.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
}

