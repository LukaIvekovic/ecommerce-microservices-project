package hr.fer.ecommerce.payment.service;

import hr.fer.ecommerce.payment.client.OrderClient;
import hr.fer.ecommerce.payment.client.OrderDto;
import hr.fer.ecommerce.payment.dto.PaymentDto;
import hr.fer.ecommerce.payment.dto.PaymentRequestDto;
import hr.fer.ecommerce.payment.dto.PaymentStatusUpdateDto;
import hr.fer.ecommerce.payment.exception.DuplicatePaymentException;
import hr.fer.ecommerce.payment.exception.FinancialAgencyUnavailableException;
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
    private final FinancialAgencyService financialAgencyService;

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
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new DuplicatePaymentException(request.getOrderId());
        }

        String transactionId = "TXN-" + UUID.randomUUID();

        OrderDto order = orderClient.getOrder(request.getOrderId());

        if (!financialAgencyService.validateFinaAvailability()) {
            throw new FinancialAgencyUnavailableException();
        }

        if (!financialAgencyService.validatePaymentMethod(
                request.getPaymentMethod().toString(),
                request.getCardLastFourDigits())) {
            throw new IllegalArgumentException("Invalid payment method or card details");
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .paidCustomerName(request.getPaidCustomerName())
                .paidCustomerEmail(request.getPaidCustomerEmail())
                .paidAmount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .paymentProvider("FINA")
                .cardLastFourDigits(request.getCardLastFourDigits())
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Created payment: {} for order: {}", savedPayment.getId(), savedPayment.getOrderId());
        return PaymentMapper.toDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public void validatePaymentReadiness(PaymentRequestDto request) {
        log.info("Validating payment readiness for order: {}", request.getOrderId());

        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new DuplicatePaymentException(request.getOrderId());
        }

        if (!financialAgencyService.validateFinaAvailability()) {
            throw new FinancialAgencyUnavailableException();
        }

        if (!financialAgencyService.validatePaymentMethod(
                request.getPaymentMethod().toString(),
                request.getCardLastFourDigits())) {
            throw new IllegalArgumentException("Invalid payment method or card details");
        }

        log.info("Payment validation passed for order: {}", request.getOrderId());
    }

    @Transactional
    public PaymentDto preAuthorizePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        boolean authorized = financialAgencyService.preAuthorizePayment(
            payment.getTransactionId(),
            payment.getPaidAmount()
        );

        if (!authorized) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Pre-authorization failed");
            paymentRepository.save(payment);
            throw new FinancialAgencyUnavailableException(
                "Pre-authorization failed"
            );
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Payment {} pre-authorized successfully", paymentId);
        return PaymentMapper.toDTO(updatedPayment);
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

    @Transactional
    public PaymentDto sagaRefund(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        paymentRepository.deleteById(id);
        return PaymentMapper.toDTO(payment);
    }

    @Transactional
    public PaymentDto preparePayment(PaymentRequestDto request) {
        log.info("2PC PREPARE: Pre-authorizing payment for order: {}", request.getOrderId());

        validatePaymentReadiness(request);

        OrderDto order = orderClient.getOrder(request.getOrderId());

        if (!financialAgencyService.validateFinaAvailability()) {
            throw new FinancialAgencyUnavailableException();
        }

        if (!financialAgencyService.validatePaymentMethod(
                request.getPaymentMethod().toString(),
                request.getCardLastFourDigits())) {
            throw new IllegalArgumentException("Invalid payment method or card details");
        }

        // Creates payment in PRE_AUTHORIZED state (simulating funds hold)
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .paidCustomerName(request.getPaidCustomerName())
                .paidCustomerEmail(request.getPaidCustomerEmail())
                .paidAmount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PRE_AUTHORIZED)
                .paymentProvider(request.getPaymentProvider() != null ? request.getPaymentProvider() : "FINA")
                .cardLastFourDigits(request.getCardLastFourDigits())
                .transactionId("PRE-" + UUID.randomUUID())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("2PC PREPARE: Payment pre-authorized with ID: {} (status: PRE_AUTHORIZED)", savedPayment.getId());
        return PaymentMapper.toDTO(savedPayment);
    }

    @Transactional
    public void commitPayment(Long id) {
        log.info("2PC COMMIT: Capturing payment ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (payment.getStatus() != PaymentStatus.PRE_AUTHORIZED) {
            throw new IllegalStateException(
                "Cannot commit payment in status: " + payment.getStatus() + ". Expected PRE_AUTHORIZED."
            );
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());
        payment.setTransactionId(payment.getTransactionId().replace("PRE-", "CAPTURED-"));

        paymentRepository.save(payment);
        log.info("2PC COMMIT: Payment captured - ID: {}, new status: COMPLETED", id);
    }

    @Transactional
    public void abortPreparedPayment(Long id) {
        log.info("2PC ABORT: Releasing pre-authorized payment ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (payment.getStatus() != PaymentStatus.PRE_AUTHORIZED) {
            log.warn("Payment {} is not in PRE_AUTHORIZED state, current status: {}", id, payment.getStatus());
            return;
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason("Transaction aborted during 2PC");

        paymentRepository.save(payment);
        log.info("2PC ABORT: Payment authorization released - ID: {}", id);
    }
}

