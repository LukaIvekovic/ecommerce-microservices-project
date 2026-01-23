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
import hr.fer.ecommerce.gateway.dto.SagaContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaService {

    private final MicroserviceClient microserviceClient;

    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        log.info("Starting order placement for customer: {}", request.getCustomerEmail());
        SagaContext saga = SagaContext.builder().build();
        try {
            log.info("Step 1: Creating order...");
            OrderResponse order = createOrder(request);
            saga.setOrder(order);
            log.info("Order created with ID: {}", order.getId());

            log.info("Step 2: Processing payment...");
            PaymentResponse payment = createPayment(request, order);
            saga.setPayment(payment);
            log.info("Payment created with ID: {} and transaction ID: {}", payment.getId(), payment.getTransactionId());

            log.info("Step 3: Creating shipment...");
            ShipmentResponse shipment = createShipment(request, order);
            saga.setShipment(shipment);
            log.info("Shipment created with ID: {}", shipment.getId());

            PlaceOrderResponse response = PlaceOrderResponse.builder()
                    .success(true)
                    .message("Order placed successfully")
                    .orderId(order.getId())
                    .orderStatus(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .transactionId(payment.getTransactionId())
                    .shipmentId(shipment.getId())
                    .shipmentStatus(shipment.getStatus())
                    .trackingNumber(shipment.getTrackingNumber())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Order placement completed successfully for order ID: {}", order.getId());
            return response;

        } catch (Exception e) {
            log.error("Error during order placement: {}", e.getMessage(), e);
            rollback(request, saga);
            return PlaceOrderResponse.builder()
                    .success(false)
                    .message("Failed to place order")
                    .errorDetails(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private void rollback(PlaceOrderRequest request, SagaContext saga) {

        // Rollback in reverse order
        if (saga.getShipment() != null) {
            try {
                microserviceClient.cancelShipment(saga.getShipment().getId());
            } catch (Exception e) {
                log.error("Failed to compensate shipment", e);
            }
        }

        if (saga.getPayment() != null) {
            try {
                microserviceClient.refundPayment(saga.getPayment().getId());
            } catch (Exception e) {
                log.error("Failed to compensate payment", e);
            }
        }

        if (saga.getOrder() != null) {
            try {
                microserviceClient.cancelOrder(saga.getOrder().getId());
            } catch (Exception e) {
                log.error("Failed to compensate order", e);
            }
        }
    }

    private OrderResponse createOrder(PlaceOrderRequest request) {
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
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

        return microserviceClient.createOrder(orderRequest);
    }

    private PaymentResponse createPayment(PlaceOrderRequest request, OrderResponse order) {
        CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
                .orderId(order.getId())
                .paidCustomerName(request.getCustomerName())
                .paidCustomerEmail(request.getCustomerEmail())
                .paymentMethod(request.getPaymentMethod())
                .paymentProvider(request.getPaymentProvider())
                .cardLastFourDigits(request.getCardLastFourDigits())
                .build();

        return microserviceClient.createPayment(paymentRequest);
    }

    private ShipmentResponse createShipment(PlaceOrderRequest request, OrderResponse order) {
        CreateShipmentRequest shipmentRequest = CreateShipmentRequest.builder()
                .orderId(order.getId())
                .carrier(request.getCarrier())
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(7))
                .build();

        return microserviceClient.createShipment(shipmentRequest);
    }

    private List<CreateOrderRequest.OrderItem> mapOrderItems(PlaceOrderRequest request) {
        return request.getOrderItems().stream()
                .map(i -> CreateOrderRequest.OrderItem.builder()
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .build())
                .toList();
    }
}

