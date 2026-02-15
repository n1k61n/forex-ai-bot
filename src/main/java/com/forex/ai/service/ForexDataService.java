package com.forex.ai.service;

import com.forex.ai.model.ForexData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Forex Data Service
 * In a real project, this would connect to a JForex API or data feed.
 * For testing, it generates simulated data.
 */
@Slf4j
@Service
public class ForexDataService {

    private final Random random = new Random();

    /**
     * Generate test data for EUR/USD (simulation)
     * In a real project, this method would get data from the JForex API.
     */
    public ForexData generateSimulatedData(String pair) {
        log.debug("📊 Generating simulation data for {}...", pair);

        // Base price (for EUR/USD)
        double basePrice = getBasePrice(pair);

        // Generate random RSI (between 25-75, to match real market)
        double rsi = 25 + random.nextDouble() * 50;

        // Price change simulation
        double priceVariation = (random.nextDouble() - 0.5) * 0.0100;
        double close = basePrice + priceVariation;
        double open = close - (random.nextDouble() - 0.5) * 0.0020;
        double high = Math.max(open, close) + random.nextDouble() * 0.0015;
        double low = Math.min(open, close) - random.nextDouble() * 0.0015;
        double volume = 8000 + random.nextDouble() * 20000;

        // Calculate EMA (simplified)
        double emaFast = close + (random.nextDouble() - 0.5) * 0.0010;
        double emaSlow = close + (random.nextDouble() - 0.5) * 0.0020;

        // Calculate MACD
        double macd = emaFast - emaSlow;
        double macdSignal = macd + (random.nextDouble() - 0.5) * 0.0005;

        // Bollinger Bands (20 period, 2 std)
        double stdDev = 0.0030 + random.nextDouble() * 0.0020;
        double bbUpper = close + 2 * stdDev;
        double bbLower = close - 2 * stdDev;

        // ATR (Average True Range)
        double atr = 0.0010 + random.nextDouble() * 0.0030;

        return ForexData.builder()
                .pair(pair)
                .open(round(open, 5))
                .high(round(high, 5))
                .low(round(low, 5))
                .close(round(close, 5))
                .volume(round(volume, 0))
                .rsi(round(rsi, 2))
                .macd(round(macd, 5))
                .macdSignal(round(macdSignal, 5))
                .emaFast(round(emaFast, 5))
                .emaSlow(round(emaSlow, 5))
                .bbUpper(round(bbUpper, 5))
                .bbLower(round(bbLower, 5))
                .atr(round(atr, 5))
                .timestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    /**
     * Overbought scenario - SELL signal expected
     */
    public ForexData generateOverboughtData(String pair) {
        double basePrice = getBasePrice(pair);
        return ForexData.builder()
                .pair(pair)
                .open(basePrice + 0.0080)
                .high(basePrice + 0.0100)
                .low(basePrice + 0.0060)
                .close(basePrice + 0.0090)
                .volume(12000)
                .rsi(76.5)           // Very high RSI → SELL
                .macd(0.0030)
                .macdSignal(0.0022)
                .emaFast(basePrice + 0.0088)
                .emaSlow(basePrice + 0.0070)
                .bbUpper(basePrice + 0.0095)
                .bbLower(basePrice + 0.0045)
                .atr(0.0025)
                .timestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    /**
     * Oversold scenario - BUY signal expected
     */
    public ForexData generateOversoldData(String pair) {
        double basePrice = getBasePrice(pair);
        return ForexData.builder()
                .pair(pair)
                .open(basePrice - 0.0080)
                .high(basePrice - 0.0060)
                .low(basePrice - 0.0100)
                .close(basePrice - 0.0090)
                .volume(18000)
                .rsi(28.3)           // Very low RSI → BUY
                .macd(-0.0020)
                .macdSignal(-0.0015)
                .emaFast(basePrice - 0.0088)
                .emaSlow(basePrice - 0.0070)
                .bbUpper(basePrice - 0.0045)
                .bbLower(basePrice - 0.0095)
                .atr(0.0022)
                .timestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    /**
     * Neutral scenario - HOLD signal expected
     */
    public ForexData generateNeutralData(String pair) {
        double basePrice = getBasePrice(pair);
        return ForexData.builder()
                .pair(pair)
                .open(basePrice + 0.0001)
                .high(basePrice + 0.0010)
                .low(basePrice - 0.0008)
                .close(basePrice + 0.0002)
                .volume(9000)
                .rsi(51.2)           // Neutral RSI → HOLD
                .macd(0.0001)
                .macdSignal(0.0001)
                .emaFast(basePrice + 0.0002)
                .emaSlow(basePrice + 0.0001)
                .bbUpper(basePrice + 0.0080)
                .bbLower(basePrice - 0.0078)
                .atr(0.0015)
                .timestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    /**
     * Base price for the currency pair
     */
    private double getBasePrice(String pair) {
        return switch (pair.toUpperCase()) {
            case "EURUSD" -> 1.0850;
            case "GBPUSD" -> 1.2650;
            case "USDJPY" -> 149.50;
            case "USDCHF" -> 0.8850;
            case "AUDUSD" -> 0.6550;
            default -> 1.0000;
        };
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
