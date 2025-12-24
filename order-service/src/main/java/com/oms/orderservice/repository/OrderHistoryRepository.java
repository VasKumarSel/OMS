package com.oms.orderservice.repository;

import com.oms.orderservice.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    List<OrderHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);

    List<OrderHistory> findByOrderIdOrderByChangedAtAsc(Long orderId);
}
