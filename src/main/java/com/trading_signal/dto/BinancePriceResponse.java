package com.trading_signal.dto;

import lombok.Data;

@Data
public class BinancePriceResponse {

    private String symbol;

    private String price;

}