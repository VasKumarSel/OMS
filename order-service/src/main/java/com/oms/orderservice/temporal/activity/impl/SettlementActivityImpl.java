package com.oms.orderservice.temporal.activity.impl;

import com.oms.orderservice.entity.Order;
import com.oms.orderservice.entity.UserWallet;
import com.oms.orderservice.entity.UserWalletHistory;
import com.oms.orderservice.enums.OrderSide;
import com.oms.orderservice.enums.OrderStatus;
import com.oms.orderservice.enums.TransactionType;
import com.oms.orderservice.repository.OrderRepository;
import com.oms.orderservice.repository.UserWalletHistoryRepository;
import com.oms.orderservice.repository.UserWalletRepository;
import com.oms.orderservice.service.OrderService;
import com.oms.orderservice.temporal.activity.SettlementActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementActivityImpl implements SettlementActivity {

    private final OrderRepository orderRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserWalletHistoryRepository userWalletHistoryRepository;

    @Override
    public void settleOrder(Long orderId) {
        log.info("Starting order settlement for order ID: {}", orderId);

        try {
            // Get the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            // Validate order is ready for settlement
            if (order.getStatus() != OrderStatus.FILLED) {
                throw new IllegalStateException("Order is not in FILLED status, cannot settle");
            }

            if (order.getExecutionPrice() == null || order.getFilledQuantity() == null) {
                throw new IllegalStateException("Order execution details are missing");
            }

            // Get user wallet
            UserWallet wallet = userWalletRepository.findByUserId(order.getUserId())
                    .orElseThrow(() -> new IllegalStateException("User wallet not found for user: " + order.getUserId()));

            BigDecimal tradeValue = order.getExecutionPrice().multiply(new BigDecimal(order.getFilledQuantity()));
            BigDecimal totalCost = tradeValue.add(order.getFees() != null ? order.getFees() : BigDecimal.ZERO);

            BigDecimal balanceBefore = wallet.getBalance();
            BigDecimal balanceAfter;
            TransactionType transactionType;
            String description;

            // Process based on order side
            switch (order.getSide()) {
                case BUY -> {
                    // Debit wallet for purchase
                    balanceAfter = balanceBefore.subtract(totalCost);
                    if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("Insufficient funds for order settlement. Required: $" + totalCost + ", Available: $" + balanceBefore);
                    }
                    transactionType = TransactionType.ORDER_DEBIT;
                    description = String.format("Purchase of %d shares of %s at $%s (includes fees)",
                            order.getFilledQuantity(), order.getSymbol(), order.getExecutionPrice());
                }
                case SELL -> {
                    // Credit wallet for sale (minus fees)
                    BigDecimal netProceeds = tradeValue.subtract(order.getFees() != null ? order.getFees() : BigDecimal.ZERO);
                    balanceAfter = balanceBefore.add(netProceeds);
                    transactionType = TransactionType.ORDER_CREDIT;
                    description = String.format("Sale of %d shares of %s at $%s (net of fees)",
                            order.getFilledQuantity(), order.getSymbol(), order.getExecutionPrice());
                }
                default -> throw new IllegalStateException("Unknown order side: " + order.getSide());
            }

            // Update wallet balance and history
            wallet.setBalance(balanceAfter);
            userWalletRepository.save(wallet);

            UserWalletHistory history = new UserWalletHistory();
            history.setUserId(order.getUserId());
            history.setTransactionType(transactionType);
            history.setAmount(order.getSide() == OrderSide.BUY ? totalCost.negate() : tradeValue);
            history.setBalanceBefore(balanceBefore);
            history.setBalanceAfter(balanceAfter);
            history.setOrderId(orderId);
            history.setDescription(description);
            userWalletHistoryRepository.save(history);

            log.info("Order {} settled successfully. Balance: ${} -> ${}",
                    orderId, balanceBefore, balanceAfter);
        } catch (Exception e) {
            log.error("Order settlement failed for order ID {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Settlement failed: " + e.getMessage(), e);
        }

    }

    @Override
    public void compensateOrder(Long orderId, String reason) {

    }
}
