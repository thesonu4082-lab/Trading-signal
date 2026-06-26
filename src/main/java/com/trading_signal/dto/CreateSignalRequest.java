package com.trading_signal.dto;

import com.trading_signal.enums.Direction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateSignalRequest {

    @NotNull(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Direction is required")
    private Direction direction;

    @NotNull(message = "Entry price is required")
    @Positive(message = "Entry price must be greater than 0")
    private BigDecimal entryPrice;

    @NotNull(message = "Stop loss is required")
    @Positive(message = "Stop loss must be greater than 0")
    private BigDecimal stopLoss;

    @NotNull(message = "Target price is required")
    @Positive(message = "Target price must be greater than 0")
    private BigDecimal targetPrice;

    @NotNull(message = "Entry time is required")
    private LocalDateTime entryTime;

    @NotNull(message = "Expiry time is required")
    private LocalDateTime expiryTime;

}