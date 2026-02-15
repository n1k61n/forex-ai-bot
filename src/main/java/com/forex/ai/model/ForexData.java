package com.forex.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forex Data Model
 * All indicators needed for trading
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForexData {

    // Basic OHLCV data
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    // Technical Indicators
    private double rsi;          // Relative Strength Index (0-100)
    private double macd;         // MACD line
    private double macdSignal;   // MACD signal line
    private double emaFast;      // EMA 12 (fast)
    private double emaSlow;      // EMA 26 (slow)
    private double bbUpper;      // Bollinger Band upper
    private double bbLower;      // Bollinger Band lower
    private double atr;          // Average True Range

    // Currency pair
    private String pair;         // E.g., EURUSD, GBPUSD

    // Time
    private String timestamp;
}
