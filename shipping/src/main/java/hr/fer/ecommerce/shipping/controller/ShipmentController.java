package hr.fer.ecommerce.shipping.controller;

import hr.fer.ecommerce.shipping.dto.ShipmentDto;
import hr.fer.ecommerce.shipping.dto.ShipmentRequestDto;
import hr.fer.ecommerce.shipping.dto.ShipmentStatusUpdateDto;
import hr.fer.ecommerce.shipping.dto.TrackingNumberUpdateDto;
import hr.fer.ecommerce.shipping.model.ShipmentStatus;
import hr.fer.ecommerce.shipping.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Validated
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseEntity<Page<ShipmentDto>> getAllShipments(Pageable pageable) {
        return ResponseEntity.ok(shipmentService.getAllShipments(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentDto> getShipmentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrderId(orderId));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentDto> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getShipmentByTrackingNumber(trackingNumber));
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<Page<ShipmentDto>> getShipmentsByCustomerEmail(
            @PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(shipmentService.getShipmentsByCustomerEmail(email, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ShipmentDto>> getShipmentsByStatus(@PathVariable ShipmentStatus status) {
        return ResponseEntity.ok(shipmentService.getShipmentsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<ShipmentDto> createShipment(@RequestBody @Valid ShipmentRequestDto request) {
        ShipmentDto shipment = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(shipment);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentDto> updateShipmentStatus(
            @PathVariable Long id,
            @RequestBody @Valid ShipmentStatusUpdateDto request) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, request));
    }

    @PatchMapping("/{id}/tracking")
    public ResponseEntity<ShipmentDto> updateTrackingNumber(
            @PathVariable Long id,
            @RequestBody @Valid TrackingNumberUpdateDto request) {
        return ResponseEntity.ok(shipmentService.updateTrackingNumber(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateShipment(@RequestBody @Valid ShipmentRequestDto request) {
        shipmentService.validateShipmentReadiness(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelShipment(@PathVariable Long id) {
        shipmentService.sagaCancel(id);
        return ResponseEntity.ok().build();
    }

    // 2PC endpoints
    @PostMapping("/prepare")
    public ResponseEntity<ShipmentDto> prepareShipment(@RequestBody @Valid ShipmentRequestDto request) {
        ShipmentDto shipment = shipmentService.prepareShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(shipment);
    }

    @PostMapping("/{id}/commit")
    public ResponseEntity<Void> commitShipment(@PathVariable Long id) {
        shipmentService.commitShipment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/abort")
    public ResponseEntity<Void> abortShipment(@PathVariable Long id) {
        shipmentService.abortPreparedShipment(id);
        return ResponseEntity.ok().build();
    }
}

