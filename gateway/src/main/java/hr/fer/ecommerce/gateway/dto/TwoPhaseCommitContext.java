package hr.fer.ecommerce.gateway.dto;

import hr.fer.ecommerce.gateway.client.order.OrderResponse;
import hr.fer.ecommerce.gateway.client.payment.PaymentResponse;
import hr.fer.ecommerce.gateway.client.shipment.ShipmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwoPhaseCommitContext {
    private OrderResponse order;
    private PaymentResponse payment;
    private ShipmentResponse shipment;

    private boolean orderPrepared;
    private boolean paymentPrepared;
    private boolean shipmentPrepared;

    private boolean orderCommitted;
    private boolean paymentCommitted;
    private boolean shipmentCommitted;

    private long orderLatency;
    private long paymentLatency;
    private long shippingLatency;
    private long totalLatency;
    private long prepareLatency;
    private long commitLatency;
    private long abortLatency;


    // helper metoda za zbrajanje ukupne latencije commit faze
    public void addToTotalLatency(long delta) {
        this.totalLatency += delta;
    }
}
