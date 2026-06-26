package com.trading_signal.repository;

import com.trading_signal.entity.TradingSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {

}