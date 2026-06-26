package com.trading_signal.client;

import com.trading_signal.dto.BinancePriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class BinanceClient {

    private final WebClient webClient;

    public BinancePriceResponse getCurrentPrice(String symbol) {

        return webClient.get()
                .uri("https://api.binance.com/api/v3/ticker/price?symbol=" + symbol)
                .retrieve()
                .bodyToMono(BinancePriceResponse.class)
                .block();

    }

}