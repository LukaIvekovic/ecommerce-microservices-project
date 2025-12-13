package hr.fer.ecommerce.payment.repository;

import hr.fer.ecommerce.payment.model.Payment;
import hr.fer.ecommerce.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
    Page<Payment> findByPaidCustomerEmail(String customerEmail, Pageable pageable);
    List<Payment> findByStatus(PaymentStatus status);
}

