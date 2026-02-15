package com.forex.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Proqnoz Nəticəsi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResult {

    // Siqnal: BUY, SELL, HOLD
    private String signal;

    // Ehtimallar (%)
    private double buyProbability;
    private double sellProbability;
    private double holdProbability;

    // Əminlik səviyyəsi (%)
    private double confidence;

    // Trade etmək tövsiyəsi
    private boolean shouldTrade;

    // Əlavə məlumat
    private String reason;
    private String pair;
    private String timestamp;

    // Risk səviyyəsi
    private String riskLevel; // LOW, MEDIUM, HIGH
}
