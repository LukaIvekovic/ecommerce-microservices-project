package hr.fer.ecommerce.payment.service;

import hr.fer.ecommerce.payment.client.OrderClient;
import hr.fer.ecommerce.payment.client.OrderDto;
import hr.fer.ecommerce.payment.dto.PaymentDto;
import hr.fer.ecommerce.payment.dto.PaymentRequestDto;
import hr.fer.ecommerce.payment.dto.PaymentStatusUpdateDto;
import hr.fer.ecommerce.payment.exception.PaymentNotFoundException;
import hr.fer.ecommerce.payment.mapper.PaymentMapper;
import hr.fer.ecommerce.payment.model.Payment;
import hr.fer.ecommerce.payment.model.PaymentStatus;
import hr.fer.ecommerce.payment.repository.PaymentRepository;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(PaymentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return PaymentMapper.toDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order id: " + orderId));
        return PaymentMapper.toDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for transaction id: " + transactionId));
        return PaymentMapper.toDTO(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentsByCustomerEmail(String email, Pageable pageable) {
        return paymentRepository.findByPaidCustomerEmail(email, pageable)
                .map(PaymentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(PaymentMapper::toDTO)
                .toList();
    }

    @Transactional
    public PaymentDto createPayment(PaymentRequestDto request) {
        String transactionId = "TXN-" + UUID.randomUUID();

        OrderDto order = orderClient.getOrder(request.getOrderId());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .paidCustomerName(request.getPaidCustomerName())
                .paidCustomerEmail(request.getPaidCustomerEmail())
                .paidAmount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .paymentProvider(request.getPaymentProvider() != null ? request.getPaymentProvider() : "Default Provider")
                .cardLastFourDigits(request.getCardLastFourDigits())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Created payment: {} for order: {}", savedPayment.getId(), savedPayment.getOrderId());
        return PaymentMapper.toDTO(savedPayment);
    }

    @Transactional
    public PaymentDto updatePaymentStatus(Long id, PaymentStatusUpdateDto request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        payment.setStatus(request.getStatus());

        if (request.getFailureReason() != null) {
            payment.setFailureReason(request.getFailureReason());
        }

        if (request.getStatus() == PaymentStatus.COMPLETED ||
            request.getStatus() == PaymentStatus.FAILED) {
            payment.setProcessedAt(LocalDateTime.now());
        }

        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Updated payment {} status to {}", id, request.getStatus());
        return PaymentMapper.toDTO(updatedPayment);
    }

    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException(id);
        }
        paymentRepository.deleteById(id);
        log.info("Deleted payment: {}", id);
    }
}

