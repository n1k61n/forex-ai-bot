package com.forex.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forex Məlumat Modeli
 * Trading üçün lazım olan bütün indikatolar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForexData {

    // Əsas OHLCV məlumatları
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    // Texniki İndikatorlar
    private double rsi;          // Relative Strength Index (0-100)
    private double macd;         // MACD xətti
    private double macdSignal;   // MACD siqnal xətti
    private double emaFast;      // EMA 12 (sürətli)
    private double emaSlow;      // EMA 26 (yavaş)
    private double bbUpper;      // Bollinger Band yuxarı
    private double bbLower;      // Bollinger Band aşağı
    private double atr;          // Average True Range

    // Valyuta cütü
    private String pair;         // Məs: EURUSD, GBPUSD

    // Zaman
    private String timestamp;
}
