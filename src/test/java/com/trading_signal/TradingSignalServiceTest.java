package com.trading_signal;

import com.trading_signal.client.BinanceClient;
import com.trading_signal.dto.BinancePriceResponse;
import com.trading_signal.dto.CreateSignalRequest;
import com.trading_signal.entity.TradingSignal;
import com.trading_signal.enums.Direction;
import com.trading_signal.enums.SignalStatus;
import com.trading_signal.exception.BadRequestException;
import com.trading_signal.repository.TradingSignalRepository;
import com.trading_signal.service.TradingSignalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradingSignalServiceTest {

    @Mock
    private TradingSignalRepository repository;

    @Mock
    private BinanceClient binanceClient;

    @InjectMocks
    private TradingSignalServiceImpl service;

    private TradingSignal openSignal;

    @BeforeEach
    void setUp() {
        openSignal = TradingSignal.builder()
            .id(1L)
            .symbol("BTCUSDT")
            .direction(Direction.BUY)
            .entryPrice(BigDecimal.valueOf(60000))
            .stopLoss(BigDecimal.valueOf(58000))
            .targetPrice(BigDecimal.valueOf(65000))
            .entryTime(LocalDateTime.now().minusHours(1))
            .expiryTime(LocalDateTime.now().plusHours(23))
            .createdAt(LocalDateTime.now())
            .status(SignalStatus.OPEN)
            .build();
    }

    // ==================
    // BUY Validation
    // ==================

    @Test
    void buySignal_invalidStopLoss_throwsException() {
        CreateSignalRequest request = new CreateSignalRequest();
        request.setSymbol("BTCUSDT");
        request.setDirection(Direction.BUY);
        request.setEntryPrice(BigDecimal.valueOf(60000));
        request.setStopLoss(BigDecimal.valueOf(62000)); // Wrong
        request.setTargetPrice(BigDecimal.valueOf(65000));
        request.setEntryTime(LocalDateTime.now().minusHours(1));
        request.setExpiryTime(LocalDateTime.now().plusHours(23));

        assertThrows(BadRequestException.class,
            () -> service.createSignal(request));
    }

    @Test
    void buySignal_invalidTargetPrice_throwsException() {
        CreateSignalRequest request = new CreateSignalRequest();
        request.setSymbol("BTCUSDT");
        request.setDirection(Direction.BUY);
        request.setEntryPrice(BigDecimal.valueOf(60000));
        request.setStopLoss(BigDecimal.valueOf(58000));
        request.setTargetPrice(BigDecimal.valueOf(55000)); // Wrong
        request.setEntryTime(LocalDateTime.now().minusHours(1));
        request.setExpiryTime(LocalDateTime.now().plusHours(23));

        assertThrows(BadRequestException.class,
            () -> service.createSignal(request));
    }

    // ==================
    // SELL Validation
    // ==================

    @Test
    void sellSignal_invalidStopLoss_throwsException() {
        CreateSignalRequest request = new CreateSignalRequest();
        request.setSymbol("ETHUSDT");
        request.setDirection(Direction.SELL);
        request.setEntryPrice(BigDecimal.valueOf(3000));
        request.setStopLoss(BigDecimal.valueOf(2800)); // Wrong
        request.setTargetPrice(BigDecimal.valueOf(2500));
        request.setEntryTime(LocalDateTime.now().minusHours(1));
        request.setExpiryTime(LocalDateTime.now().plusHours(23));

        assertThrows(BadRequestException.class,
            () -> service.createSignal(request));
    }

    // ==================
    // Time Validation
    // ==================

    @Test
    void signal_expiryBeforeEntry_throwsException() {
        CreateSignalRequest request = new CreateSignalRequest();
        request.setSymbol("BTCUSDT");
        request.setDirection(Direction.BUY);
        request.setEntryPrice(BigDecimal.valueOf(60000));
        request.setStopLoss(BigDecimal.valueOf(58000));
        request.setTargetPrice(BigDecimal.valueOf(65000));
        request.setEntryTime(LocalDateTime.now());
        request.setExpiryTime(LocalDateTime.now().minusHours(1)); // Wrong

        assertThrows(BadRequestException.class,
            () -> service.createSignal(request));
    }

    // ==================
    // Status Logic
    // ==================

    @Test
    void buySignal_targetHit() {
        when(repository.findById(1L))
            .thenReturn(Optional.of(openSignal));

        BinancePriceResponse price = new BinancePriceResponse();
        price.setPrice("66000"); // Above target 65000
        when(binanceClient.getCurrentPrice("BTCUSDT"))
            .thenReturn(price);

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TradingSignal result = service.getSignalStatus(1L);

        assertEquals(SignalStatus.TARGET_HIT, result.getStatus());
    }

    @Test
    void buySignal_stopLossHit() {
        when(repository.findById(1L))
            .thenReturn(Optional.of(openSignal));

        BinancePriceResponse price = new BinancePriceResponse();
        price.setPrice("57000"); // Below stoploss 58000
        when(binanceClient.getCurrentPrice("BTCUSDT"))
            .thenReturn(price);

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TradingSignal result = service.getSignalStatus(1L);

        assertEquals(SignalStatus.STOPLOSS_HIT, result.getStatus());
    }

    @Test
    void signal_expired() {
        openSignal.setExpiryTime(
            LocalDateTime.now().minusHours(1) // Already expired
        );

        when(repository.findById(1L))
            .thenReturn(Optional.of(openSignal));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TradingSignal result = service.getSignalStatus(1L);

        assertEquals(SignalStatus.EXPIRED, result.getStatus());
    }

    // ==================
    // ROI Calculation
    // ==================

    @Test
    void buySignal_roiCalculation() {
        when(repository.findById(1L))
            .thenReturn(Optional.of(openSignal));

        BinancePriceResponse price = new BinancePriceResponse();
        price.setPrice("63000");
        when(binanceClient.getCurrentPrice("BTCUSDT"))
            .thenReturn(price);

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TradingSignal result = service.getSignalStatus(1L);

        assertEquals(5.0, result.getRealizedRoi().doubleValue());
    }

    @Test
    void finalState_doesNotChange() {
        openSignal.setStatus(SignalStatus.TARGET_HIT);

        when(repository.findById(1L))
            .thenReturn(Optional.of(openSignal));

        TradingSignal result = service.getSignalStatus(1L);

        assertEquals(SignalStatus.TARGET_HIT, result.getStatus());
        verify(binanceClient, never())
            .getCurrentPrice(any());
    }
}