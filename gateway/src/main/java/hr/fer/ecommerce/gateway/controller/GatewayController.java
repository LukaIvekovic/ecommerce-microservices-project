package hr.fer.ecommerce.gateway.controller;

import hr.fer.ecommerce.gateway.dto.PlaceOrderRequest;
import hr.fer.ecommerce.gateway.dto.PlaceOrderResponse;
import hr.fer.ecommerce.gateway.service.SagaService;
import hr.fer.ecommerce.gateway.service.TwoPhaseCommitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = "*")
public class GatewayController {

    private final SagaService sagaService;
    private final TwoPhaseCommitService twoPhaseCommitService;

    @PostMapping("/place-order-saga")
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @RequestBody @Valid PlaceOrderRequest request) {

        log.info("Received place order request for customer: {}", request.getCustomerEmail());

        // ✅ MORA BITI OVDJE
        long startTime = System.nanoTime();

        PlaceOrderResponse response = sagaService.placeOrder(request);

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        log.info("GATEWAY | SAGA | totalDuration={} ms | success={}",
                durationMs, response.isSuccess());

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/place-order-2pc")
    public ResponseEntity<PlaceOrderResponse> placeOrderWith2PC(
            @RequestBody @Valid PlaceOrderRequest request) {

        log.info("Received 2PC place order request for customer: {}", request.getCustomerEmail());

        // ✅ I OVDJE
        long startTime = System.nanoTime();

        PlaceOrderResponse response = twoPhaseCommitService.placeOrderWith2PC(request);

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        log.info("GATEWAY | 2PC | totalDuration={} ms | success={}",
                durationMs, response.isSuccess());

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
