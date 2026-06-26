package com.trading_signal.controller;

import com.trading_signal.dto.CreateSignalRequest;
import com.trading_signal.entity.TradingSignal;
import com.trading_signal.service.TradingSignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
public class TradingSignalController {

    private final TradingSignalService service;

    @PostMapping
    public TradingSignal createSignal(@Valid @RequestBody CreateSignalRequest request) {
        return service.createSignal(request);
    }

    @GetMapping
    public List<TradingSignal> getAllSignals() {
        return service.getAllSignals();
    }

    @GetMapping("/{id}")
    public TradingSignal getSignalById(@PathVariable Long id) {
        return service.getSignalById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteSignal(@PathVariable Long id) {
        service.deleteSignal(id);
    }

    @GetMapping("/{id}/status")
public TradingSignal getSignalStatus(@PathVariable Long id) {
    return service.getSignalStatus(id);
}
}