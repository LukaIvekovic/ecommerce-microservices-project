package hr.fer.ecommerce.shipping.repository;

import hr.fer.ecommerce.shipping.model.Shipment;
import hr.fer.ecommerce.shipping.model.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Page<Shipment> findByCustomerEmail(String customerEmail, Pageable pageable);
    List<Shipment> findByStatus(ShipmentStatus status);
}

