package com.forex.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Prediction Result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResult {

    // Signal: BUY, SELL, HOLD
    private String signal;

    // Probabilities (%)
    private double buyProbability;
    private double sellProbability;
    private double holdProbability;

    // Confidence level (%)
    private double confidence;

    // Recommendation to trade
    private boolean shouldTrade;

    // Additional information
    private String reason;
    private String pair;
    private String timestamp;

    // Risk level
    private String riskLevel; // LOW, MEDIUM, HIGH
}
