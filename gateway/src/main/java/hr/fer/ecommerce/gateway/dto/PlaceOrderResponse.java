package hr.fer.ecommerce.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderResponse {

    private boolean success;
    private String message;

    private Long orderId;
    private String orderStatus;
    private BigDecimal totalAmount;

    private Long paymentId;
    private String paymentStatus;
    private String transactionId;

    private Long shipmentId;
    private String shipmentStatus;
    private String trackingNumber;

    private LocalDateTime timestamp;

    private String errorDetails;

    private long orderLatency;
    private long paymentLatency;
    private long shippingLatency;
    private long totalLatency;
    private int compensations;
    private long prepareLatency;
    private long commitLatency;
    private long abortLatency;
    private int global2pcRollbacks;   // 2PC rollback

}

