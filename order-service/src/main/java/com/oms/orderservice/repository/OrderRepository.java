package com.oms.orderservice.repository;

import com.oms.orderservice.entity.Order;
import com.oms.orderservice.enums.OrderStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndSymbol(Long userId, String symbol, Pageable pageable);

    Page<Order> findByUserIdAndStatusAndSymbol(Long userId, OrderStatus status, String symbol, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findBySymbol(String symbol, Pageable pageable);

    Page<Order> findByStatusAndSymbol(OrderStatus status, String symbol, Pageable pageable);
}
