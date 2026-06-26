package com.trading_signal.service;

import com.trading_signal.client.BinanceClient;
import com.trading_signal.dto.BinancePriceResponse;
import com.trading_signal.dto.CreateSignalRequest;
import com.trading_signal.entity.TradingSignal;
import com.trading_signal.enums.Direction;
import com.trading_signal.enums.SignalStatus;
import com.trading_signal.exception.BadRequestException;
import com.trading_signal.exception.ResourceNotFoundException;
import com.trading_signal.repository.TradingSignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradingSignalServiceImpl implements TradingSignalService {

    private final TradingSignalRepository repository;
    private final BinanceClient binanceClient;

    @Override
    public TradingSignal createSignal(CreateSignalRequest request) {

        if (request.getDirection() == Direction.BUY) {
            if (request.getStopLoss().compareTo(request.getEntryPrice()) >= 0) {
                throw new BadRequestException(
                    "For BUY signal Stop Loss must be less than Entry Price");
            }
            if (request.getTargetPrice().compareTo(request.getEntryPrice()) <= 0) {
                throw new BadRequestException(
                    "For BUY signal Target Price must be greater than Entry Price");
            }
        }

        if (request.getDirection() == Direction.SELL) {
            if (request.getStopLoss().compareTo(request.getEntryPrice()) <= 0) {
                throw new BadRequestException(
                    "For SELL signal Stop Loss must be greater than Entry Price");
            }
            if (request.getTargetPrice().compareTo(request.getEntryPrice()) >= 0) {
                throw new BadRequestException(
                    "For SELL signal Target Price must be less than Entry Price");
            }
        }

        if (request.getExpiryTime().isBefore(request.getEntryTime())) {
            throw new BadRequestException(
                "Expiry time must be after Entry time");
        }

        if (request.getEntryTime().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new BadRequestException(
                "Entry time cannot be older than 24 hours");
        }

        TradingSignal signal = TradingSignal.builder()
            .symbol(request.getSymbol())
            .direction(request.getDirection())
            .entryPrice(request.getEntryPrice())
            .stopLoss(request.getStopLoss())
            .targetPrice(request.getTargetPrice())
            .entryTime(request.getEntryTime())
            .expiryTime(request.getExpiryTime())
            .createdAt(LocalDateTime.now())
            .status(SignalStatus.OPEN)
            .build();

        return repository.save(signal);
    }

    @Override
    public List<TradingSignal> getAllSignals() {
        return repository.findAll();
    }

    @Override
    public TradingSignal getSignalById(Long id) {
        return repository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Signal not found with id : " + id));
    }

    @Override
    public void deleteSignal(Long id) {
        TradingSignal signal = repository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Signal not found with id : " + id));
        repository.delete(signal);
    }

    @Override
    public TradingSignal getSignalStatus(Long id) {

        TradingSignal signal = repository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Signal not found with id : " + id));

        if (signal.getStatus() == SignalStatus.TARGET_HIT ||
            signal.getStatus() == SignalStatus.STOPLOSS_HIT ||
            signal.getStatus() == SignalStatus.EXPIRED) {
            return signal;
        }

        if (LocalDateTime.now().isAfter(signal.getExpiryTime())) {
            signal.setStatus(SignalStatus.EXPIRED);
            return repository.save(signal);
        }

        BinancePriceResponse priceResponse =
            binanceClient.getCurrentPrice(signal.getSymbol());
        double currentPrice =
            Double.parseDouble(priceResponse.getPrice());

        double roi;

        if (signal.getDirection() == Direction.BUY) {
            roi = (currentPrice - signal.getEntryPrice().doubleValue())
                / signal.getEntryPrice().doubleValue() * 100;
            if (currentPrice >= signal.getTargetPrice().doubleValue()) {
                signal.setStatus(SignalStatus.TARGET_HIT);
            } else if (currentPrice <= signal.getStopLoss().doubleValue()) {
                signal.setStatus(SignalStatus.STOPLOSS_HIT);
            }
        } else {
            roi = (signal.getEntryPrice().doubleValue() - currentPrice)
                / signal.getEntryPrice().doubleValue() * 100;
            if (currentPrice <= signal.getTargetPrice().doubleValue()) {
                signal.setStatus(SignalStatus.TARGET_HIT);
            } else if (currentPrice >= signal.getStopLoss().doubleValue()) {
                signal.setStatus(SignalStatus.STOPLOSS_HIT);
            }
        }

        signal.setRealizedRoi(
    java.math.BigDecimal.valueOf(
        Math.round(roi * 100.0) / 100.0
    )
);
        return repository.save(signal);
    }
}