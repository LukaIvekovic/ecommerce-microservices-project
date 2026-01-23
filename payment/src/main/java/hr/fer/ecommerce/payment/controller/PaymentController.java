package hr.fer.ecommerce.payment.controller;

import hr.fer.ecommerce.payment.dto.PaymentDto;
import hr.fer.ecommerce.payment.dto.PaymentRequestDto;
import hr.fer.ecommerce.payment.dto.PaymentStatusUpdateDto;
import hr.fer.ecommerce.payment.model.PaymentStatus;
import hr.fer.ecommerce.payment.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<PaymentDto>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentDto> getPaymentByTransactionId(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPaymentByTransactionId(transactionId));
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<Page<PaymentDto>> getPaymentsByCustomerEmail(
            @PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomerEmail(email, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@RequestBody @Valid PaymentRequestDto request) {
        PaymentDto payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentDto> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody @Valid PaymentStatusUpdateDto request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validatePayment(@RequestBody @Valid PaymentRequestDto request) {
        paymentService.validatePaymentReadiness(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pre-authorize")
    public ResponseEntity<PaymentDto> preAuthorizePayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.preAuthorizePayment(id));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentDto> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.sagaRefund(id));
    }
}

