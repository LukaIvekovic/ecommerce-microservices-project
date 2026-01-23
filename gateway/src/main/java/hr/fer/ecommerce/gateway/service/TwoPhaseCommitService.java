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
            commitPhase(request, context);

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
        CreateOrderRequest orderRequest = buildOrderRequest(request);

        log.info("Prepare Phase Step 1: Validate stock availability");
        microserviceClient.validateStockAvailability(orderRequest);
        context.setOrderPrepared(true);
        log.info("Stock validation passed");

        log.info("Prepare Phase Step 2: Validate payment");
        CreatePaymentRequest paymentRequest = buildPaymentRequest(request);
        microserviceClient.validatePayment(paymentRequest);
        context.setPaymentPrepared(true);
        log.info("Payment validation passed");

        log.info("Prepare Phase Step 3: Validate shipment");
        CreateShipmentRequest shipmentRequest = buildShipmentRequest(request);
        microserviceClient.validateShipment(shipmentRequest);
        context.setShipmentPrepared(true);
        log.info("Shipment validation passed");

        log.info("=== PREPARE PHASE COMPLETED SUCCESSFULLY ===");
    }

    private void commitPhase(PlaceOrderRequest request, TwoPhaseCommitContext context) {
        log.info("Commit Phase Step 1: Create order");
        CreateOrderRequest orderRequest = buildOrderRequest(request);
        OrderResponse order = microserviceClient.createOrder(orderRequest);
        context.setOrder(order);
        context.setOrderCommitted(true);
        log.info("Order {} created", order.getId());

        log.info("Commit Phase Step 2: Create payment");
        CreatePaymentRequest paymentRequest = buildPaymentRequest(request, order);
        PaymentResponse payment = microserviceClient.createPayment(paymentRequest);
        context.setPayment(payment);
        context.setPaymentCommitted(true);
        log.info("Payment {} created", payment.getId());

        log.info("Commit Phase Step 3: Create shipment");
        CreateShipmentRequest shipmentRequest = buildShipmentRequest(request, order);
        ShipmentResponse shipment = microserviceClient.createShipment(shipmentRequest);
        context.setShipment(shipment);
        context.setShipmentCommitted(true);
        log.info("Shipment {} created", shipment.getId());

        log.info("=== COMMIT PHASE COMPLETED SUCCESSFULLY ===");
    }

    private void abortPhase(TwoPhaseCommitContext context) {
        log.warn("=== ABORT PHASE: Rolling back 2PC transaction ===");

        if (context.getShipment() != null) {
            try {
                log.warn("Aborting shipment {}", context.getShipment().getId());
                microserviceClient.cancelShipment(context.getShipment().getId());
            } catch (Exception e) {
                log.error("Failed to abort shipment", e);
            }
        }

        if (context.getPayment() != null) {
            try {
                log.warn("Aborting payment {}", context.getPayment().getId());
                microserviceClient.refundPayment(context.getPayment().getId());
            } catch (Exception e) {
                log.error("Failed to abort payment", e);
            }
        }

        if (context.getOrder() != null) {
            try {
                log.warn("Aborting order {}", context.getOrder().getId());
                microserviceClient.cancelOrder(context.getOrder().getId());
            } catch (Exception e) {
                log.error("Failed to abort order", e);
            }
        }


        log.warn("=== ABORT PHASE COMPLETED ===");
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

    private CreatePaymentRequest buildPaymentRequest(PlaceOrderRequest request) {
        return CreatePaymentRequest.builder()
                .paidCustomerName(request.getCustomerName())
                .paidCustomerEmail(request.getCustomerEmail())
                .paymentMethod(request.getPaymentMethod())
                .paymentProvider(request.getPaymentProvider())
                .cardLastFourDigits(request.getCardLastFourDigits())
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

    private CreateShipmentRequest buildShipmentRequest(PlaceOrderRequest request) {
        return CreateShipmentRequest.builder()
                .carrier(request.getCarrier())
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(7))
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
