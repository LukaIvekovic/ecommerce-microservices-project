package hr.fer.ecommerce.gateway.service;

import hr.fer.ecommerce.gateway.client.MicroserviceClient;
import hr.fer.ecommerce.gateway.dto.PlaceOrderRequest;
import hr.fer.ecommerce.gateway.dto.PlaceOrderResponse;
import hr.fer.ecommerce.gateway.client.order.CreateOrderRequest;
import hr.fer.ecommerce.gateway.client.order.OrderResponse;
import hr.fer.ecommerce.gateway.client.payment.CreatePaymentRequest;
import hr.fer.ecommerce.gateway.client.payment.PaymentResponse;
import hr.fer.ecommerce.gateway.client.shipment.CreateShipmentRequest;
import hr.fer.ecommerce.gateway.client.shipment.ShipmentResponse;
import hr.fer.ecommerce.gateway.dto.TwoPhaseCommitContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoPhaseCommitService {

    private final MicroserviceClient microserviceClient;

    public PlaceOrderResponse placeOrderWith2PC(PlaceOrderRequest request) {
        log.info("Starting 2PC order placement for customer: {}", request.getCustomerEmail());
        TwoPhaseCommitContext context = TwoPhaseCommitContext.builder().build();

        try {
            log.info("=== PHASE 1: PREPARE ===");
            preparePhase(request, context);

            log.info("=== PHASE 2: COMMIT ===");
            commitPhase(context);

            PlaceOrderResponse response = PlaceOrderResponse.builder()
                    .success(true)
                    .message("Order placed successfully using 2PC protocol")
                    .orderId(context.getOrder().getId())
                    .orderStatus(context.getOrder().getStatus())
                    .totalAmount(context.getOrder().getTotalAmount())
                    .paymentId(context.getPayment().getId())
                    .paymentStatus(context.getPayment().getStatus())
                    .transactionId(context.getPayment().getTransactionId())
                    .shipmentId(context.getShipment().getId())
                    .shipmentStatus(context.getShipment().getStatus())
                    .trackingNumber(context.getShipment().getTrackingNumber())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("2PC order placement completed successfully for order ID: {}", context.getOrder().getId());
            return response;

        } catch (Exception e) {
            log.error("Error during 2PC order placement: {}", e.getMessage(), e);
            abortPhase(context);
            return PlaceOrderResponse.builder()
                    .success(false)
                    .message("Failed to place order using 2PC protocol")
                    .errorDetails(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private void preparePhase(PlaceOrderRequest request, TwoPhaseCommitContext context) {
        log.info("========================================");
        log.info("Starting PREPARE phase - All participants will reserve resources");
        log.info("========================================");

        CreateOrderRequest orderRequest = buildOrderRequest(request);

        // Step 1: Prepare Order (validate and reserve stock)
        log.info("Prepare Phase Step 1: Preparing order and reserving stock");
        OrderResponse preparedOrder = microserviceClient.prepareOrder(orderRequest);
        context.setOrder(preparedOrder);
        context.setOrderPrepared(true);
        log.info("✓ Order prepared - ID: {}, Status: PREPARED (stock reserved)", preparedOrder.getId());

        // Step 2: Prepare Payment (pre-authorize funds)
        log.info("Prepare Phase Step 2: Pre-authorizing payment");
        CreatePaymentRequest paymentRequest = buildPaymentRequest(request, preparedOrder);
        PaymentResponse preparedPayment = microserviceClient.preparePayment(paymentRequest);
        context.setPayment(preparedPayment);
        context.setPaymentPrepared(true);
        log.info("✓ Payment pre-authorized - ID: {}, Status: PRE_AUTHORIZED (funds held)", preparedPayment.getId());

        // Step 3: Prepare Shipment (reserve carrier capacity)
        log.info("Prepare Phase Step 3: Reserving shipment capacity");
        CreateShipmentRequest shipmentRequest = buildShipmentRequest(request, preparedOrder);
        ShipmentResponse preparedShipment = microserviceClient.prepareShipment(shipmentRequest);
        context.setShipment(preparedShipment);
        context.setShipmentPrepared(true);
        log.info("✓ Shipment reserved - ID: {}, Status: RESERVED (carrier capacity held)", preparedShipment.getId());

        log.info("========================================");
        log.info("=== PREPARE PHASE COMPLETED ===");
        log.info("All participants voted READY - Resources are locked and reserved");
        log.info("========================================");
    }

    private void commitPhase(TwoPhaseCommitContext context) {
        log.info("========================================");
        log.info("Starting COMMIT phase - Finalizing all prepared transactions");
        log.info("========================================");

        // Commit Phase Step 1: Commit Order (PREPARED -> CONFIRMED)
        log.info("Commit Phase Step 1: Committing order");
        microserviceClient.commitOrder(context.getOrder().getId());
        context.setOrderCommitted(true);
        log.info("✓ Order committed - ID: {}, Status: CONFIRMED", context.getOrder().getId());

        // Commit Phase Step 2: Commit Payment (PRE_AUTHORIZED -> COMPLETED)
        log.info("Commit Phase Step 2: Capturing payment");
        microserviceClient.commitPayment(context.getPayment().getId());
        context.setPaymentCommitted(true);
        log.info("✓ Payment captured - ID: {}, Status: COMPLETED", context.getPayment().getId());

        // Commit Phase Step 3: Commit Shipment (RESERVED -> PREPARING)
        log.info("Commit Phase Step 3: Confirming shipment");
        microserviceClient.commitShipment(context.getShipment().getId());
        context.setShipmentCommitted(true);
        log.info("✓ Shipment confirmed - ID: {}, Status: PREPARING", context.getShipment().getId());

        log.info("========================================");
        log.info("=== COMMIT PHASE COMPLETED ===");
        log.info("All participants committed successfully - Transaction is atomic and consistent");
        log.info("========================================");
    }

    private void abortPhase(TwoPhaseCommitContext context) {
        log.warn("========================================");
        log.warn("=== ABORT PHASE ===");
        log.warn("Rolling back all prepared transactions and releasing resources");
        log.warn("========================================");

        // Abort in reverse order of preparation to maintain consistency

        // Abort Shipment if prepared (release carrier capacity)
        if (context.isShipmentPrepared() && context.getShipment() != null) {
            try {
                log.warn("Aborting shipment {} (releasing carrier capacity)", context.getShipment().getId());
                microserviceClient.abortShipment(context.getShipment().getId());
                log.warn("✓ Shipment aborted - ID: {}", context.getShipment().getId());
            } catch (Exception e) {
                log.error("Failed to abort shipment: {}", e.getMessage(), e);
            }
        }

        // Abort Payment if prepared (release pre-authorization)
        if (context.isPaymentPrepared() && context.getPayment() != null) {
            try {
                log.warn("Aborting payment {} (releasing pre-authorization)", context.getPayment().getId());
                microserviceClient.abortPayment(context.getPayment().getId());
                log.warn("✓ Payment aborted - ID: {}", context.getPayment().getId());
            } catch (Exception e) {
                log.error("Failed to abort payment: {}", e.getMessage(), e);
            }
        }

        // Abort Order if prepared (release stock reservation)
        if (context.isOrderPrepared() && context.getOrder() != null) {
            try {
                log.warn("Aborting order {} (releasing stock reservation)", context.getOrder().getId());
                microserviceClient.abortOrder(context.getOrder().getId());
                log.warn("✓ Order aborted - ID: {}", context.getOrder().getId());
            } catch (Exception e) {
                log.error("Failed to abort order: {}", e.getMessage(), e);
            }
        }

        log.warn("========================================");
        log.warn("=== ABORT PHASE COMPLETED ===");
        log.warn("All resources released - Transaction rolled back");
        log.warn("========================================");
    }

    private CreateOrderRequest buildOrderRequest(PlaceOrderRequest request) {
        return CreateOrderRequest.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(request.getShippingAddress())
                .orderItems(request.getOrderItems().stream()
                        .map(item -> CreateOrderRequest.OrderItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private CreatePaymentRequest buildPaymentRequest(PlaceOrderRequest request, OrderResponse order) {
        return CreatePaymentRequest.builder()
                .orderId(order.getId())
                .paidCustomerName(request.getCustomerName())
                .paidCustomerEmail(request.getCustomerEmail())
                .paymentMethod(request.getPaymentMethod())
                .paymentProvider(request.getPaymentProvider())
                .cardLastFourDigits(request.getCardLastFourDigits())
                .build();
    }

    private CreateShipmentRequest buildShipmentRequest(PlaceOrderRequest request, OrderResponse order) {
        return CreateShipmentRequest.builder()
                .orderId(order.getId())
                .carrier(request.getCarrier())
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(7))
                .build();
    }
}
