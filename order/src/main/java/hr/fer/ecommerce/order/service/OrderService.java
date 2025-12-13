package hr.fer.ecommerce.order.service;

import hr.fer.ecommerce.order.client.ProductClient;
import hr.fer.ecommerce.order.client.ProductDto;
import hr.fer.ecommerce.order.dto.OrderDto;
import hr.fer.ecommerce.order.dto.OrderItemRequestDto;
import hr.fer.ecommerce.order.dto.OrderRequestDto;
import hr.fer.ecommerce.order.dto.OrderStatusUpdateDto;
import hr.fer.ecommerce.order.exception.OrderNotFoundException;
import hr.fer.ecommerce.order.mapper.OrderMapper;
import hr.fer.ecommerce.order.model.Order;
import hr.fer.ecommerce.order.model.OrderItem;
import hr.fer.ecommerce.order.model.OrderStatus;
import hr.fer.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderMapper.toDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByCustomerEmail(String email, Pageable pageable) {
        return orderRepository.findByCustomerEmail(email, pageable)
                .map(OrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @Transactional
    public OrderDto createOrder(OrderRequestDto request) {
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(request.getShippingAddress())
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Create order items by fetching product information
        for (OrderItemRequestDto itemRequest : request.getOrderItems()) {
            ProductDto product = productClient.getProduct(itemRequest.getProductId());

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        log.info("Created order: {}", savedOrder.getId());
        return OrderMapper.toDTO(savedOrder);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatusUpdateDto request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);

        log.info("Updated order {} status to {}", id, request.getStatus());
        return OrderMapper.toDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }
        orderRepository.deleteById(id);
        log.info("Deleted order: {}", id);
    }
}

