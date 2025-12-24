package com.oms.orderservice.controller;

import com.oms.orderservice.dto.CreateOrderRequest;
import com.oms.orderservice.dto.OrderHistoryResponse;
import com.oms.orderservice.dto.OrderResponse;
import com.oms.orderservice.entity.User;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Creating order for user: {}, request: {}", currentUser.getUsername(), request);

        OrderResponse response = orderService.createOrder(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.debug("Getting order {} for user: {}", orderId, currentUser.getUsername());

        OrderResponse response = orderService.getOrderById(orderId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.debug("Getting orders for user: {}, status: {}, symbol: {}, page: {}, size: {}",
                currentUser.getUsername(), status, symbol, page, size);

        // Validate page and size parameters
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> response = orderService.getOrders(status, symbol, currentUser, pageable);
        return ResponseEntity.ok(response.getContent());
    }

    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(
            @PathVariable Long orderId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.debug("Getting order history for order {} by user: {}", orderId, currentUser.getUsername());

        List<OrderHistoryResponse> response = orderService.getOrderHistory(orderId, currentUser);
        return ResponseEntity.ok(response);
    }


}
