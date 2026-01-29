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

        long totalStart = System.currentTimeMillis();
        long prepareLatency = 0;
        long commitLatency = 0;
        int compensations = 0;

        try {
            // --- PREPARE ---
            long startPrepare = System.currentTimeMillis();
            preparePhase(request, context);  // priprema servise
            prepareLatency = System.currentTimeMillis() - startPrepare;

        } catch (Exception e) {
            log.error("Prepare phase failed: {}", e.getMessage());

            // rollback svih pripremljenih resursa
            compensations = abortPhase(context);

            // čak i kod fail-a želimo pripremnu fazu
            prepareLatency = context.getOrderLatency() + context.getPaymentLatency() + context.getShippingLatency();

            long totalLatency = System.currentTimeMillis() - totalStart;

            return PlaceOrderResponse.builder()
                    .success(false)
                    .message("Order preparation failed - transaction aborted")
                    .errorDetails(parseErrorMessage(e))
                    .timestamp(LocalDateTime.now())
                    .orderLatency(context.getOrderLatency())
                    .paymentLatency(context.getPaymentLatency())
                    .shippingLatency(context.getShippingLatency())
                    .prepareLatency(prepareLatency)
                    .commitLatency(commitLatency)
                    .abortLatency(context.getAbortLatency())
                    .totalLatency(totalLatency)
                    .compensations(compensations)
                    .build();

        }

        // --- COMMIT ---
        long startCommit = System.currentTimeMillis();
        commitPhase(context);
        commitLatency = System.currentTimeMillis() - startCommit;

        long totalLatency = System.currentTimeMillis() - totalStart;

        return PlaceOrderResponse.builder()
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
                .orderLatency(context.getOrderLatency())
                .paymentLatency(context.getPaymentLatency())
                .shippingLatency(context.getShippingLatency())
                .prepareLatency(prepareLatency)
                .commitLatency(commitLatency)
                .totalLatency(totalLatency)
                .compensations(compensations)
                .build();
    }

    // Pomoćna metoda da iz exception-a dobiješ samo relevantnu poruku
    private String parseErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg.contains("FINA")) return "FINA service unavailable";
        if (msg.contains("Carrier")) return "Shipping capacity unavailable";
        return "Unexpected error: " + msg;
    }





    private void preparePhase(PlaceOrderRequest request, TwoPhaseCommitContext context) {
        // --- ORDER ---
        long startOrder = System.currentTimeMillis();
        CreateOrderRequest orderRequest = buildOrderRequest(request);
        log.info("Preparing order and reserving stock");
        OrderResponse preparedOrder = microserviceClient.prepareOrder(orderRequest);
        context.setOrder(preparedOrder);
        context.setOrderPrepared(true);
        context.setOrderLatency(System.currentTimeMillis() - startOrder);
        log.info("Order prepared: ID={}, status={}, latency={}ms",
                preparedOrder.getId(), preparedOrder.getStatus(), context.getOrderLatency());

        // --- PAYMENT ---
        long startPayment = System.currentTimeMillis();
        log.info("Pre-authorizing payment");
        CreatePaymentRequest paymentRequest = buildPaymentRequest(request, preparedOrder);
        PaymentResponse preparedPayment = microserviceClient.preparePayment(paymentRequest);
        context.setPayment(preparedPayment);
        context.setPaymentPrepared(true);
        context.setPaymentLatency(System.currentTimeMillis() - startPayment);
        log.info("Payment prepared: ID={}, status={}, latency={}ms",
                preparedPayment.getId(), preparedPayment.getStatus(), context.getPaymentLatency());

        // --- SHIPMENT ---
        long startShipment = System.currentTimeMillis();
        log.info("Reserving shipment capacity");
        CreateShipmentRequest shipmentRequest = buildShipmentRequest(request, preparedOrder);
        ShipmentResponse preparedShipment = microserviceClient.prepareShipment(shipmentRequest);
        context.setShipment(preparedShipment);
        context.setShipmentPrepared(true);
        context.setShippingLatency(System.currentTimeMillis() - startShipment);
        log.info("Shipment prepared: ID={}, status={}, latency={}ms",
                preparedShipment.getId(), preparedShipment.getStatus(), context.getShippingLatency());

        log.info("Prepare phase completed - all resources reserved");
    }

    private void commitPhase(TwoPhaseCommitContext context) {
        // --- ORDER ---
        long startOrderCommit = System.currentTimeMillis();
        log.info("Committing order");
        OrderResponse committedOrder = microserviceClient.commitOrder(context.getOrder().getId());
        context.setOrder(committedOrder);
        context.setOrderCommitted(true);
        context.addToTotalLatency(System.currentTimeMillis() - startOrderCommit);
        log.info("Order committed: ID={}, latency={}ms", context.getOrder().getId(), System.currentTimeMillis() - startOrderCommit);

        // --- PAYMENT ---
        long startPaymentCommit = System.currentTimeMillis();
        log.info("Committing payment");
        PaymentResponse committedPayment = microserviceClient.commitPayment(context.getPayment().getId());
        context.setPayment(committedPayment);
        context.setPaymentCommitted(true);
        context.addToTotalLatency(System.currentTimeMillis() - startPaymentCommit);
        log.info("Payment committed: ID={}, latency={}ms", context.getPayment().getId(), System.currentTimeMillis() - startPaymentCommit);

        // --- SHIPMENT ---
        long startShipmentCommit = System.currentTimeMillis();
        log.info("Committing shipment");
        ShipmentResponse committedShipment = microserviceClient.commitShipment(context.getShipment().getId());
        context.setShipment(committedShipment);
        context.setShipmentCommitted(true);
        context.addToTotalLatency(System.currentTimeMillis() - startShipmentCommit);
        log.info("Shipment committed: ID={}, latency={}ms", context.getShipment().getId(), System.currentTimeMillis() - startShipmentCommit);

        log.info("Commit phase completed");
    }


    private int abortPhase(TwoPhaseCommitContext context) {
        long startAbort = System.currentTimeMillis();
        int compensations = 0;

        if (context.isShipmentPrepared() && context.getShipment() != null) {
            try {
                microserviceClient.abortShipment(context.getShipment().getId());
                compensations++;
            } catch (Exception e) {
                log.error("Failed to abort shipment: {}", e.getMessage());
            }
        }

        if (context.isPaymentPrepared() && context.getPayment() != null) {
            try {
                microserviceClient.abortPayment(context.getPayment().getId());
                compensations++;
            } catch (Exception e) {
                log.error("Failed to abort payment: {}", e.getMessage());
            }
        }

        if (context.isOrderPrepared() && context.getOrder() != null) {
            try {
                microserviceClient.abortOrder(context.getOrder().getId());
                compensations++;
            } catch (Exception e) {
                log.error("Failed to abort order: {}", e.getMessage());
            }
        }

        context.setAbortLatency(System.currentTimeMillis() - startAbort);
        log.info("Abort phase completed in {} ms with {} compensations", context.getAbortLatency(), compensations);

        return compensations;
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
