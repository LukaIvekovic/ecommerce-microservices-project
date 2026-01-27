package hr.fer.ecommerce.shipping.service;

import hr.fer.ecommerce.shipping.client.OrderClient;
import hr.fer.ecommerce.shipping.client.OrderDto;
import hr.fer.ecommerce.shipping.dto.ShipmentDto;
import hr.fer.ecommerce.shipping.dto.ShipmentRequestDto;
import hr.fer.ecommerce.shipping.dto.ShipmentStatusUpdateDto;
import hr.fer.ecommerce.shipping.dto.TrackingNumberUpdateDto;
import hr.fer.ecommerce.shipping.exception.CarrierUnavailableException;
import hr.fer.ecommerce.shipping.exception.DuplicateShipmentException;
import hr.fer.ecommerce.shipping.exception.ShipmentNotFoundException;
import hr.fer.ecommerce.shipping.mapper.ShipmentMapper;
import hr.fer.ecommerce.shipping.model.Shipment;
import hr.fer.ecommerce.shipping.model.ShipmentStatus;
import hr.fer.ecommerce.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderClient orderClient;
    private final CarrierService carrierService;

    @Transactional(readOnly = true)
    public Page<ShipmentDto> getAllShipments(Pageable pageable) {
        return shipmentRepository.findAll(pageable)
                .map(ShipmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ShipmentDto getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
        return ShipmentMapper.toDTO(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentDto getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found for order: " + orderId));
        return ShipmentMapper.toDTO(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentDto getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with tracking number: " + trackingNumber));
        return ShipmentMapper.toDTO(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentDto> getShipmentsByCustomerEmail(String email, Pageable pageable) {
        return shipmentRepository.findByCustomerEmail(email, pageable)
                .map(ShipmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentDto> getShipmentsByStatus(ShipmentStatus status) {
        return shipmentRepository.findByStatus(status).stream()
                .map(ShipmentMapper::toDTO)
                .toList();
    }

    @Transactional
    public ShipmentDto createShipment(ShipmentRequestDto request) {
        if (shipmentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new DuplicateShipmentException(request.getOrderId());
        }

        OrderDto order = orderClient.getOrder(request.getOrderId());

        if (!carrierService.validateCarrierAvailability(request.getCarrier())) {
            throw new CarrierUnavailableException(request.getCarrier());
        }

        if (!carrierService.validateShippingAddress(request.getCarrier(), order.getShippingAddress())) {
            throw new IllegalArgumentException("Invalid shipping address for carrier: " + request.getCarrier());
        }

        if (!carrierService.checkCarrierCapacity(request.getCarrier())) {
            throw new CarrierUnavailableException(request.getCarrier(), "Carrier at full capacity");
        }

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .carrier(request.getCarrier())
                .trackingNumber(generateTrackingNumber())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .status(ShipmentStatus.PREPARING)
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        log.info("Created shipment: {} for order: {}", savedShipment.getId(), request.getOrderId());
        return ShipmentMapper.toDTO(savedShipment);
    }

    @Transactional(readOnly = true)
    public void validateShipmentReadiness(ShipmentRequestDto request) {
        log.info("Validating shipment readiness for order: {}", request.getOrderId());

        OrderDto order = orderClient.getOrder(request.getOrderId());

        if (!carrierService.validateCarrierAvailability(request.getCarrier())) {
            throw new CarrierUnavailableException(request.getCarrier());
        }

        if (!carrierService.validateShippingAddress(request.getCarrier(), order.getShippingAddress())) {
            throw new IllegalArgumentException("Invalid shipping address for carrier: " + request.getCarrier());
        }

        if (!carrierService.checkCarrierCapacity(request.getCarrier())) {
            throw new CarrierUnavailableException(request.getCarrier(), "Carrier at full capacity");
        }

        log.info("Shipment validation passed for order: {}", request.getOrderId());
    }

    @Transactional
    public ShipmentDto updateShipmentStatus(Long id, ShipmentStatusUpdateDto request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));

        shipment.setStatus(request.getStatus());

        if (request.getStatus() == ShipmentStatus.DELIVERED && shipment.getActualDeliveryDate() == null) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);

        log.info("Updated shipment {} status to {}", id, request.getStatus());
        return ShipmentMapper.toDTO(updatedShipment);
    }

    @Transactional
    public ShipmentDto updateTrackingNumber(Long id, TrackingNumberUpdateDto request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));

        shipment.setTrackingNumber(request.getTrackingNumber());
        Shipment updatedShipment = shipmentRepository.save(shipment);

        log.info("Updated shipment {} tracking number", id);
        return ShipmentMapper.toDTO(updatedShipment);
    }

    @Transactional
    public void deleteShipment(Long id) {
        long startTime = System.nanoTime();

        if (!shipmentRepository.existsById(id)) {
            throw new ShipmentNotFoundException(id);
        }

        shipmentRepository.deleteById(id);

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Deleted shipment: {} in {} ms", id, duration);
    }

    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public void sagaCancel(Long id) {
        long startTime = System.nanoTime();

        if (!shipmentRepository.existsById(id)) {
            throw new ShipmentNotFoundException(id);
        }

        shipmentRepository.deleteById(id);

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.warn("Saga compensation: cancelled shipment {} in {} ms", id, duration);
    }


    @Transactional
    public ShipmentDto prepareShipment(ShipmentRequestDto request) {
        long startTime = System.nanoTime();

        log.info("2PC PREPARE: Reserving shipment for order: {}", request.getOrderId());

        validateShipmentReadiness(request);

        OrderDto order = orderClient.getOrder(request.getOrderId());

        if (!carrierService.validateCarrierAvailability(request.getCarrier())) {
            throw new CarrierUnavailableException(request.getCarrier());
        }

        if (!carrierService.validateShippingAddress(request.getCarrier(), order.getShippingAddress())) {
            throw new IllegalArgumentException("Invalid shipping address for carrier: " + request.getCarrier());
        }

        if (!carrierService.checkCarrierCapacity(request.getCarrier())) {
            throw new CarrierUnavailableException(request.getCarrier(), "Carrier at full capacity");
        }

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .carrier(request.getCarrier())
                .status(ShipmentStatus.RESERVED)
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate() != null
                    ? request.getEstimatedDeliveryDate()
                    : LocalDateTime.now().plusDays(7))
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("2PC PREPARE: Shipment reserved with ID: {} (status: RESERVED) in {} ms", savedShipment.getId(), duration);

        return ShipmentMapper.toDTO(savedShipment);
    }

    @Transactional
    public ShipmentDto commitShipment(Long id) {
        long startTime = System.nanoTime();

        log.info("2PC COMMIT: Confirming shipment ID: {}", id);
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));

        if (shipment.getStatus() != ShipmentStatus.RESERVED) {
            throw new IllegalStateException(
                "Cannot commit shipment in status: " + shipment.getStatus() + ". Expected RESERVED."
            );
        }

        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setStatus(ShipmentStatus.PREPARING);

        Shipment savedShipment = shipmentRepository.save(shipment);
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("2PC COMMIT: Shipment confirmed - ID: {}, tracking: {}, new status: PREPARING in {} ms",
                id, savedShipment.getTrackingNumber(), duration);
        return ShipmentMapper.toDTO(savedShipment);
    }

    @Transactional
    public void abortPreparedShipment(Long id) {
        long startTime = System.nanoTime();

        log.info("2PC ABORT: Releasing reserved shipment ID: {}", id);
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));

        if (shipment.getStatus() != ShipmentStatus.RESERVED) {
            log.warn("Shipment {} is not in RESERVED state, current status: {}", id, shipment.getStatus());
            return;
        }

        shipment.setStatus(ShipmentStatus.FAILED);

        shipmentRepository.save(shipment);
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("2PC ABORT: Shipment reservation released - ID: {} in {} ms", id, duration);
    }

}


