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
 * Forex AI Bot - Unit Testlər
 */
@SpringBootTest
class ForexAiApplicationTests {

    @Autowired
    private WekaModelService wekaModelService;

    @Autowired
    private ForexDataService forexDataService;

    // =========================================
    // MODEL TƏMİNATLARI
    // =========================================

    @Test
    @DisplayName("Model yüklənməlidir")
    void modelShouldBeLoaded() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);
        assertNotNull(result, "Nəticə null olmamalıdır");
    }

    @Test
    @DisplayName("Proqnoz siqnalı BUY, SELL, HOLD-dan biri olmalıdır")
    void signalShouldBeValid() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        assertTrue(
            result.getSignal().equals("BUY") ||
            result.getSignal().equals("SELL") ||
            result.getSignal().equals("HOLD"),
            "Siqnal BUY, SELL və ya HOLD olmalıdır"
        );
    }

    @Test
    @DisplayName("Ehtimallar cəmi 100% olmalıdır")
    void probabilitiesShouldSumTo100() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        double sum = result.getBuyProbability()
                   + result.getSellProbability()
                   + result.getHoldProbability();

        assertEquals(100.0, sum, 1.0, "Ehtimalların cəmi ~100% olmalıdır");
    }

    @Test
    @DisplayName("Əminlik 0-100 arasında olmalıdır")
    void confidenceShouldBeInRange() {
        ForexData data = forexDataService.generateSimulatedData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        assertTrue(result.getConfidence() >= 0 && result.getConfidence() <= 100,
            "Əminlik 0-100 arasında olmalıdır");
    }

    // =========================================
    // SSENARİ TƏMİNATLARI
    // =========================================

    @Test
    @DisplayName("Oversold data → BUY siqnalı gözlənilir")
    void oversoldShouldGenerateBuySignal() {
        // RSI = 28.3 → çox aşağı → BUY
        ForexData data = forexDataService.generateOversoldData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        System.out.println("✅ Oversold Testi:");
        System.out.println("   RSI: " + data.getRsi());
        System.out.println("   Siqnal: " + result.getSignal());
        System.out.println("   Əminlik: " + result.getConfidence() + "%");

        assertEquals("BUY", result.getSignal(),
            "RSI 28.3 olduqda BUY siqnalı gözlənilir");
    }

    @Test
    @DisplayName("Overbought data → SELL siqnalı gözlənilir")
    void overboughtShouldGenerateSellSignal() {
        // RSI = 76.5 → çox yüksək → SELL
        ForexData data = forexDataService.generateOverboughtData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        System.out.println("✅ Overbought Testi:");
        System.out.println("   RSI: " + data.getRsi());
        System.out.println("   Siqnal: " + result.getSignal());
        System.out.println("   Əminlik: " + result.getConfidence() + "%");

        assertEquals("SELL", result.getSignal(),
            "RSI 76.5 olduqda SELL siqnalı gözlənilir");
    }

    @Test
    @DisplayName("Neytral data → HOLD siqnalı gözlənilir")
    void neutralShouldGenerateHoldSignal() {
        // RSI = 51.2 → neytral → HOLD
        ForexData data = forexDataService.generateNeutralData("EURUSD");
        PredictionResult result = wekaModelService.predict(data);

        System.out.println("✅ Neytral Testi:");
        System.out.println("   RSI: " + data.getRsi());
        System.out.println("   Siqnal: " + result.getSignal());
        System.out.println("   Əminlik: " + result.getConfidence() + "%");

        assertEquals("HOLD", result.getSignal(),
            "RSI 51.2 olduqda HOLD siqnalı gözlənilir");
    }

    // =========================================
    // RİSK TƏMİNATLARI
    // =========================================

    @Test
    @DisplayName("Yüksək ATR-də risk HIGH olmalıdır")
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
            .atr(0.0050)  // Çox yüksək ATR → HIGH risk
            .close(1.0850)
            .volume(10000)
            .build();

        PredictionResult result = wekaModelService.predict(data);
        assertEquals("HIGH", result.getRiskLevel(),
            "Yüksək ATR-də risk HIGH olmalıdır");
        assertFalse(result.isShouldTrade(),
            "Yüksək risklə trade etməməlidir");
    }

    // =========================================
    // ÇOXLU CÜT TƏMİNATLARI
    // =========================================

    @Test
    @DisplayName("Müxtəlif valyuta cütləri üçün proqnoz işləməlidir")
    void shouldWorkForMultiplePairs() {
        String[] pairs = {"EURUSD", "GBPUSD", "USDJPY", "AUDUSD"};

        for (String pair : pairs) {
            ForexData data = forexDataService.generateSimulatedData(pair);
            PredictionResult result = wekaModelService.predict(data);

            assertNotNull(result, pair + " üçün nəticə null olmamalıdır");
            assertNotNull(result.getSignal(), pair + " üçün siqnal null olmamalıdır");

            System.out.println("✅ " + pair + ": " + result.getSignal()
                + " (" + result.getConfidence() + "%)");
        }
    }
}
