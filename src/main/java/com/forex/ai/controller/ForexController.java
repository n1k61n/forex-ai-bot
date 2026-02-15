package com.forex.ai.controller;

import com.forex.ai.model.ForexData;
import com.forex.ai.model.PredictionResult;
import com.forex.ai.service.ForexDataService;
import com.forex.ai.service.WekaModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forex AI REST Controller
 * Bütün API endpoint-lər buradadır
 */
@Slf4j
@RestController
@RequestMapping("/api/forex")
@RequiredArgsConstructor
public class ForexController {

    private final WekaModelService wekaModelService;
    private final ForexDataService forexDataService;

    /**
     * ✅ API Sağlamlığı yoxla
     * GET /api/forex/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "✅ Aktiv");
        response.put("service", "Forex AI Bot");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * 🔮 Öz məlumatınızla proqnoz alın
     * POST /api/forex/predict
     *
     * Body nümunəsi:
     * {
     *   "pair": "EURUSD",
     *   "rsi": 30.5,
     *   "macd": -0.0020,
     *   "macdSignal": -0.0015,
     *   "emaFast": 1.0820,
     *   "emaSlow": 1.0835,
     *   "bbUpper": 1.0900,
     *   "bbLower": 1.0750,
     *   "atr": 0.0025,
     *   "close": 1.0810,
     *   "volume": 15000
     * }
     */
    @PostMapping("/predict")
    public ResponseEntity<PredictionResult> predict(@RequestBody ForexData forexData) {
        log.info("🔮 Proqnoz sorğusu: {}", forexData.getPair());
        PredictionResult result = wekaModelService.predict(forexData);
        return ResponseEntity.ok(result);
    }

    /**
     * 🎲 Simulyasiya məlumatı ilə proqnoz
     * GET /api/forex/predict/simulate/{pair}
     *
     * Nümunə: GET /api/forex/predict/simulate/EURUSD
     */
    @GetMapping("/predict/simulate/{pair}")
    public ResponseEntity<Map<String, Object>> predictSimulated(
            @PathVariable String pair) {

        log.info("🎲 Simulyasiya sorğusu: {}", pair);

        ForexData data = forexDataService.generateSimulatedData(pair);
        PredictionResult result = wekaModelService.predict(data);

        Map<String, Object> response = new HashMap<>();
        response.put("input", data);
        response.put("prediction", result);

        return ResponseEntity.ok(response);
    }

    /**
     * 📊 Ssenari testləri
     * GET /api/forex/test/scenarios/{pair}
     *
     * 3 fərqli ssenari test edir: BUY, SELL, HOLD
     */
    @GetMapping("/test/scenarios/{pair}")
    public ResponseEntity<Map<String, Object>> testScenarios(
            @PathVariable String pair) {

        log.info("📊 Ssenari testləri: {}", pair);

        // Oversold → BUY gözlənilir
        ForexData oversold = forexDataService.generateOversoldData(pair);
        PredictionResult buyResult = wekaModelService.predict(oversold);

        // Overbought → SELL gözlənilir
        ForexData overbought = forexDataService.generateOverboughtData(pair);
        PredictionResult sellResult = wekaModelService.predict(overbought);

        // Neytral → HOLD gözlənilir
        ForexData neutral = forexDataService.generateNeutralData(pair);
        PredictionResult holdResult = wekaModelService.predict(neutral);

        Map<String, Object> scenarios = new HashMap<>();

        // BUY ssenarisi
        Map<String, Object> buyScenario = new HashMap<>();
        buyScenario.put("ssenari", "Oversold (RSI: 28.3) → BUY gözlənilir");
        buyScenario.put("input", oversold);
        buyScenario.put("netice", buyResult);
        scenarios.put("buy_ssenarisi", buyScenario);

        // SELL ssenarisi
        Map<String, Object> sellScenario = new HashMap<>();
        sellScenario.put("ssenari", "Overbought (RSI: 76.5) → SELL gözlənilir");
        sellScenario.put("input", overbought);
        sellScenario.put("netice", sellResult);
        scenarios.put("sell_ssenarisi", sellScenario);

        // HOLD ssenarisi
        Map<String, Object> holdScenario = new HashMap<>();
        holdScenario.put("ssenari", "Neytral (RSI: 51.2) → HOLD gözlənilir");
        holdScenario.put("input", neutral);
        holdScenario.put("netice", holdResult);
        scenarios.put("hold_ssenarisi", holdScenario);

        return ResponseEntity.ok(scenarios);
    }

    /**
     * 📈 Birdən çox valyuta cütü üçün eyni anda proqnoz
     * GET /api/forex/predict/all
     */
    @GetMapping("/predict/all")
    public ResponseEntity<List<Map<String, Object>>> predictAll() {

        List<String> pairs = List.of("EURUSD", "GBPUSD", "USDJPY", "AUDUSD");

        List<Map<String, Object>> results = pairs.stream().map(pair -> {
            ForexData data = forexDataService.generateSimulatedData(pair);
            PredictionResult result = wekaModelService.predict(data);

            Map<String, Object> item = new HashMap<>();
            item.put("pair", pair);
            item.put("signal", result.getSignal());
            item.put("confidence", result.getConfidence() + "%");
            item.put("shouldTrade", result.isShouldTrade());
            item.put("riskLevel", result.getRiskLevel());
            item.put("reason", result.getReason());
            return item;
        }).toList();

        return ResponseEntity.ok(results);
    }

    /**
     * 🔄 Modeli yenidən öyrət
     * POST /api/forex/model/retrain
     */
    @PostMapping("/model/retrain")
    public ResponseEntity<Map<String, String>> retrainModel() {
        log.info("🔄 Model yenidən öyrədilir...");
        wekaModelService.trainWithSampleData();

        Map<String, String> response = new HashMap<>();
        response.put("status", "✅ Model yenidən öyrədildi");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * ℹ️ API məlumatı
     * GET /api/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("ad", "Forex AI Bot");
        info.put("versiya", "1.0.0");
        info.put("texnologiya", "Spring Boot + Weka ML");
        info.put("desteklenen_cutler", List.of("EURUSD", "GBPUSD", "USDJPY", "AUDUSD", "USDCHF"));

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET  /api/forex/health", "API sağlamlığı");
        endpoints.put("POST /api/forex/predict", "Öz datanla proqnoz");
        endpoints.put("GET  /api/forex/predict/simulate/{pair}", "Simulyasiya ilə proqnoz");
        endpoints.put("GET  /api/forex/test/scenarios/{pair}", "3 ssenari testi");
        endpoints.put("GET  /api/forex/predict/all", "Bütün cütlər üçün proqnoz");
        endpoints.put("POST /api/forex/model/retrain", "Modeli yenidən öyrət");
        info.put("endpointler", endpoints);

        return ResponseEntity.ok(info);
    }
}
