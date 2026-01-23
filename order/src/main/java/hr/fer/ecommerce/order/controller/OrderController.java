package hr.fer.ecommerce.order.controller;

import hr.fer.ecommerce.order.dto.OrderDto;
import hr.fer.ecommerce.order.dto.OrderRequestDto;
import hr.fer.ecommerce.order.dto.OrderStatusUpdateDto;
import hr.fer.ecommerce.order.model.OrderStatus;
import hr.fer.ecommerce.order.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<Page<OrderDto>> getOrdersByCustomerEmail(
            @PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerEmail(email, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody @Valid OrderRequestDto request) {
        OrderDto order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody @Valid OrderStatusUpdateDto request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    // 2PC endpoints
    @PostMapping("/prepare")
    public ResponseEntity<OrderDto> prepareOrder(@RequestBody @Valid OrderRequestDto request) {
        OrderDto order = orderService.prepareOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/{id}/commit")
    public ResponseEntity<Void> commitOrder(@PathVariable Long id) {
        orderService.commitOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/abort")
    public ResponseEntity<Void> abortOrder(@PathVariable Long id) {
        orderService.abortPreparedOrder(id);
        return ResponseEntity.ok().build();
    }
}

