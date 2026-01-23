package hr.fer.ecommerce.gateway.service;

import hr.fer.ecommerce.gateway.client.MicroserviceClient;
import hr.fer.ecommerce.gateway.client.order.CreateOrderRequest;
import hr.fer.ecommerce.gateway.client.order.OrderResponse;
import hr.fer.ecommerce.gateway.client.payment.CreatePaymentRequest;
import hr.fer.ecommerce.gateway.client.payment.PaymentResponse;
import hr.fer.ecommerce.gateway.client.shipment.CreateShipmentRequest;
import hr.fer.ecommerce.gateway.client.shipment.ShipmentResponse;
import hr.fer.ecommerce.gateway.dto.PlaceOrderRequest;
import hr.fer.ecommerce.gateway.dto.PlaceOrderResponse;
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
        log.info("Starting 2PC for customer: {}", request.getCustomerEmail());
        TwoPhaseCommitContext context = TwoPhaseCommitContext.builder().build();

        try {
            log.info("PHASE 1: PREPARE");
            preparePhase(request, context);
            log.info("All participants voted READY");
        } catch (Exception e) {
            log.error("Prepare phase failed: {}", e.getMessage());
            abortPhase(context);
            return PlaceOrderResponse.builder()
                    .success(false)
                    .message("Order preparation failed - transaction aborted")
                    .errorDetails(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        log.info("PHASE 2: COMMIT");
        commitPhase(context);

        PlaceOrderResponse response = PlaceOrderResponse.builder()
                .success(true)
                .message("Order placed successfully")
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

        log.info("2PC completed successfully - Order ID: {}", context.getOrder().getId());
        return response;
    }

    private void preparePhase(PlaceOrderRequest request, TwoPhaseCommitContext context) {
        CreateOrderRequest orderRequest = buildOrderRequest(request);

        log.info("Preparing order and reserving stock");
        OrderResponse preparedOrder = microserviceClient.prepareOrder(orderRequest);
        context.setOrder(preparedOrder);
        context.setOrderPrepared(true);
        log.info("Order prepared: ID={}, status={}", preparedOrder.getId(), preparedOrder.getStatus());

        log.info("Pre-authorizing payment");
        CreatePaymentRequest paymentRequest = buildPaymentRequest(request, preparedOrder);
        PaymentResponse preparedPayment = microserviceClient.preparePayment(paymentRequest);
        context.setPayment(preparedPayment);
        context.setPaymentPrepared(true);
        log.info("Payment prepared: ID={}, status={}", preparedPayment.getId(), preparedPayment.getStatus());

        log.info("Reserving shipment capacity");
        CreateShipmentRequest shipmentRequest = buildShipmentRequest(request, preparedOrder);
        ShipmentResponse preparedShipment = microserviceClient.prepareShipment(shipmentRequest);
        context.setShipment(preparedShipment);
        context.setShipmentPrepared(true);
        log.info("Shipment prepared: ID={}, status={}", preparedShipment.getId(), preparedShipment.getStatus());

        log.info("Prepare phase completed - all resources reserved");
    }

    private void commitPhase(TwoPhaseCommitContext context) {
        log.info("Committing order");
        microserviceClient.commitOrder(context.getOrder().getId());
        context.setOrderCommitted(true);
        log.info("Order committed: ID={}", context.getOrder().getId());

        log.info("Committing payment");
        microserviceClient.commitPayment(context.getPayment().getId());
        context.setPaymentCommitted(true);
        log.info("Payment committed: ID={}", context.getPayment().getId());

        log.info("Committing shipment");
        microserviceClient.commitShipment(context.getShipment().getId());
        context.setShipmentCommitted(true);
        log.info("Shipment committed: ID={}", context.getShipment().getId());

        log.info("Commit phase completed");
    }

    private void abortPhase(TwoPhaseCommitContext context) {
        log.warn("Aborting transaction - releasing all prepared resources");

        if (context.isShipmentPrepared() && context.getShipment() != null) {
            try {
                log.warn("Aborting shipment: ID={}", context.getShipment().getId());
                microserviceClient.abortShipment(context.getShipment().getId());
                log.warn("Shipment aborted");
            } catch (Exception e) {
                log.error("Failed to abort shipment: {}", e.getMessage());
            }
        }

        if (context.isPaymentPrepared() && context.getPayment() != null) {
            try {
                log.warn("Aborting payment: ID={}", context.getPayment().getId());
                microserviceClient.abortPayment(context.getPayment().getId());
                log.warn("Payment aborted");
            } catch (Exception e) {
                log.error("Failed to abort payment: {}", e.getMessage());
            }
        }

        if (context.isOrderPrepared() && context.getOrder() != null) {
            try {
                log.warn("Aborting order: ID={}", context.getOrder().getId());
                microserviceClient.abortOrder(context.getOrder().getId());
                log.warn("Order aborted");
            } catch (Exception e) {
                log.error("Failed to abort order: {}", e.getMessage());
            }
        }

        log.warn("Abort phase completed");
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
