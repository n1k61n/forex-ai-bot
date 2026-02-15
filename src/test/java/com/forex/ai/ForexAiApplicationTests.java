package com.forex.ai;

import com.forex.ai.model.ForexData;
import com.forex.ai.model.PredictionResult;
import com.forex.ai.service.ForexDataService;
import com.forex.ai.service.WekaModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Forex AI Bot - Unit Tests
 */
@SpringBootTest
class ForexAiApplicationTests {

    @Autowired
    private WekaModelService wekaModelService;

    @Autowired
    private ForexDataService forexDataService;

    // =========================================
    // MODEL ASSURANCES
    // =========================================

    @Test
    @DisplayName("Model should be loaded")
    void modelShouldBeLoaded() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);
        assertNotNull(result, "Result should not be null");
    }

    @Test
    @DisplayName("Prediction signal should be one of BUY, SELL, HOLD")
    void signalShouldBeValid() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        assertTrue(
            result.getSignal().equals("BUY") ||
            result.getSignal().equals("SELL") ||
            result.getSignal().equals("HOLD"),
            "Signal should be BUY, SELL, or HOLD"
        );
    }

    @Test
    @DisplayName("Probabilities should sum to 100%")
    void probabilitiesShouldSumTo100() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        double sum = result.getBuyProbability()
                   + result.getSellProbability()
                   + result.getHoldProbability();

        assertEquals(100.0, sum, 1.0, "Sum of probabilities should be ~100%");
    }

    @Test
    @DisplayName("Confidence should be between 0 and 100")
    void confidenceShouldBeInRange() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        assertTrue(result.getConfidence() >= 0 && result.getConfidence() <= 100,
            "Confidence should be between 0 and 100");
    }

    // =========================================
    // SCENARIO ASSURANCES
    // =========================================

    @Test
    @DisplayName("Oversold data → BUY signal expected")
    void oversoldShouldGenerateBuySignal() {
        // RSI = 28.3 → very low → BUY
        ForexData data = forexDataService.generateOversoldData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        System.out.println("✅ Oversold Test:");
        System.out.println("   RSI: " + data.getRsi());
        System.out.println("   Signal: " + result.getSignal());
        System.out.println("   Confidence: " + result.getConfidence() + "%");

        assertEquals("BUY", result.getSignal(),
            "BUY signal is expected when RSI is 28.3");
    }

    @Test
    @DisplayName("Overbought data → SELL signal expected")
    void overboughtShouldGenerateSellSignal() {
        // RSI = 76.5 → very high → SELL
        ForexData data = forexDataService.generateOverboughtData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        System.out.println("✅ Overbought Test:");
        System.out.println("   RSI: " + data.getRsi());
        System.out.println("   Signal: " + result.getSignal());
        System.out.println("   Confidence: " + result.getConfidence() + "%");

        assertEquals("SELL", result.getSignal(),
            "SELL signal is expected when RSI is 76.5");
    }

    @Test
    @DisplayName("Neutral data → HOLD signal expected")
    void neutralShouldGenerateHoldSignal() {
        // RSI = 51.2 → neutral → HOLD
        ForexData data = forexDataService.generateNeutralData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        System.out.println("✅ Neutral Test:");
        System.out.println("   RSI: " + data.getRsi());
        System.out.println("   Signal: " + result.getSignal());
        System.out.println("   Confidence: " + result.getConfidence() + "%");

        assertEquals("HOLD", result.getSignal(),
            "HOLD signal is expected when RSI is 51.2");
    }

    // =========================================
    // RISK ASSURANCES
    // =========================================

    @Test
    @DisplayName("Risk should be HIGH on high ATR")
    void highAtrShouldBeHighRisk() {
        ForexData data = ForexData.builder()
            .pair("EURUSD")
            .rsi(50.0)
            .macd(0.0001)
            .macdSignal(0.0001)
            .emaFast(1.0850)
            .emaSlow(1.0849)
            .bbUpper(1.0920)
            .bbLower(1.0780)
            .atr(0.0050)  // Very high ATR → HIGH risk
            .close(1.0850)
            .volume(10000)
            .build();

        PredictionResult result = wekaModelService.predict(data);
        assertEquals("HIGH", result.getRiskLevel(),
            "Risk should be HIGH on high ATR");
        assertFalse(result.isShouldTrade(),
            "Should not trade with high risk");
    }

    // =========================================
    // MULTIPLE PAIR ASSURANCES
    // =========================================

    @Test
    @DisplayName("Prediction should work for different currency pairs")
    void shouldWorkForMultiplePairs() {
        String[] pairs = {"EURUSD", "GBPUSD", "USDJPY", "AUDUSD"};

        for (String pair : pairs) {
            ForexData data = forexDataService.generateSimulatedData(pair);
            PredictionResult result = wekaModelService.predict(data);

            assertNotNull(result, "Result for " + pair + " should not be null");
            assertNotNull(result.getSignal(), "Signal for " + pair + " should not be null");

            System.out.println("✅ " + pair + ": " + result.getSignal()
                + " (" + result.getConfidence() + "%)");
        }
    }
}
