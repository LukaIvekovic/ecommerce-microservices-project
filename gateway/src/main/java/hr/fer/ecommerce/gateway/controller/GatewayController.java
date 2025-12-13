package hr.fer.ecommerce.gateway.controller;

import hr.fer.ecommerce.gateway.dto.PlaceOrderRequest;
import hr.fer.ecommerce.gateway.dto.PlaceOrderResponse;
import hr.fer.ecommerce.gateway.service.OrderOrchestrationService;
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

    private final OrderOrchestrationService orderOrchestrationService;

    @PostMapping("/place-order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody @Valid PlaceOrderRequest request) {
        log.info("Received place order request for customer: {}", request.getCustomerEmail());

        PlaceOrderResponse response = orderOrchestrationService.placeOrder(request);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

