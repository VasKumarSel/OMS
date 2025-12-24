package com.oms.orderservice.service;

import com.oms.orderservice.dto.CreateOrderRequest;
import com.oms.orderservice.dto.OrderHistoryResponse;
import com.oms.orderservice.dto.OrderResponse;
import com.oms.orderservice.entity.Order;
import com.oms.orderservice.entity.OrderHistory;
import com.oms.orderservice.entity.User;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.enums.UserRole;
import com.oms.orderservice.repository.OrderHistoryRepository;
import com.oms.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderHistoryRepository orderHistoryRepository;

    private final OrderValidationService orderValidationService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User currentUser) {
        log.info("Creating order for user: {}, symbol: {}, side: {}, quantity: {}",
                currentUser.getUsername(), request.getSymbol(), request.getSide(), request.getQuantity());

//         Validate order
        orderValidationService.validateOrder(request);
        orderValidationService.validateMarketHours();

        Order order = new Order();
        order.setUserId(currentUser.getId());
        order.setSymbol(request.getSymbol().toUpperCase());
        order.setSide(request.getSide());
        order.setQuantity(request.getQuantity());
        order.setOrderType(request.getOrderType());
        order.setLimitPrice(request.getLimitPrice());
        order.setStatus(OrderStatus.PENDING);
        order.setWorkflowId(generateWorkflowId());

        Order savedOrder = orderRepository.save(order);
        // Create initial history entry
        addOrderHistory(savedOrder.getId(), OrderStatus.PENDING, "Order created");
        log.info("Order created with ID: {} and workflow ID: {}", savedOrder.getId(), savedOrder.getWorkflowId());
        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, User currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Check access permissions
        validateOrderAccess(order, currentUser);

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(OrderStatus status, String symbol, User currentUser, Pageable pageable) {
        Page<Order> orders;

        if (currentUser.getRole() == UserRole.ADMIN) {
            // Admin can see all orders
            orders = getOrdersForAdmin(status, symbol, pageable);
        } else {
            // Trader can only see their own orders
            orders = getOrdersForTrader(currentUser.getId(), status, symbol, pageable);
        }

        return orders.map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getOrderHistory(Long orderId, User currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Check access permissions
        validateOrderAccess(order, currentUser);

        List<OrderHistory> history = orderHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId);
        return history.stream()
                .map(this::mapToOrderHistoryResponse)
                .toList();
    }


    private Page<Order> getOrdersForAdmin(OrderStatus status, String symbol, Pageable pageable) {
        if (status != null && symbol != null) {
            return orderRepository.findByStatusAndSymbol(status, symbol.toUpperCase(), pageable);
        } else if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        } else if (symbol != null) {
            return orderRepository.findBySymbol(symbol.toUpperCase(), pageable);
        } else {
            return orderRepository.findAll(pageable);
        }
    }

    private Page<Order> getOrdersForTrader(Long userId, OrderStatus status, String symbol, Pageable pageable) {
        if (status != null && symbol != null) {
            return orderRepository.findByUserIdAndStatusAndSymbol(userId, status, symbol.toUpperCase(), pageable);
        } else if (status != null) {
            return orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (symbol != null) {
            return orderRepository.findByUserIdAndSymbol(userId, symbol.toUpperCase(), pageable);
        } else {
            return orderRepository.findByUserId(userId, pageable);
        }
    }

    private void validateOrderAccess(Order order, User currentUser) {
        if (currentUser.getRole() == UserRole.TRADER && !order.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only access your own orders");
        }
    }

    private String generateWorkflowId() {
        return "order-workflow-" + UUID.randomUUID().toString();
    }

    private void addOrderHistory(Long orderId, OrderStatus status, String reason) {
        OrderHistory history = new OrderHistory(orderId, status, reason);
        orderHistoryRepository.save(history);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getOrderType(),
                order.getLimitPrice(),
//                order.getFilledQuantity(),
//                order.getExecutionPrice(),
//                order.getFees(),
                order.getStatus(),
                order.getWorkflowId(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderHistoryResponse mapToOrderHistoryResponse(OrderHistory history) {
        return new OrderHistoryResponse(
                history.getId(),
                history.getOrderId(),
                history.getStatus(),
                history.getReason(),
                history.getChangedAt()
        );
    }
}
