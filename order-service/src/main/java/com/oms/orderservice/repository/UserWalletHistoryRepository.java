package com.oms.orderservice.repository;

import com.oms.orderservice.entity.UserWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWalletHistoryRepository extends JpaRepository<UserWalletHistory, Long> {

    List<UserWalletHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserWalletHistory> findByOrderId(Long orderId);
}