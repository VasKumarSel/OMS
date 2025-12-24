package com.oms.marketdata.repository;

import com.oms.marketdata.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    Optional<MarketPrice> findBySymbol(String symbol);

    @Query("SELECT DISTINCT m.symbol FROM MarketPrice m ORDER BY m.symbol")
    List<String> findAllSymbols();

    boolean existsBySymbol(String symbol);

}
