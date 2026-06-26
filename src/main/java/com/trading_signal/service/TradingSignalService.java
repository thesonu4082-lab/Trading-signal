package com.trading_signal.service;

import com.trading_signal.dto.CreateSignalRequest;
import com.trading_signal.entity.TradingSignal;

import java.util.List;

public interface TradingSignalService {

    TradingSignal createSignal(CreateSignalRequest request);

    List<TradingSignal> getAllSignals();

    TradingSignal getSignalById(Long id);

    void deleteSignal(Long id);

    TradingSignal getSignalStatus(Long id);

}