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
 * All API endpoints are here
 */
@Slf4j
@RestController
@RequestMapping("/api/forex")
@RequiredArgsConstructor
public class ForexController {

    private final WekaModelService wekaModelService;
    private final ForexDataService forexDataService;

    /**
     * ✅ Check API Health
     * GET /api/forex/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "✅ Active");
        response.put("service", "Forex AI Bot");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * 🔮 Get prediction with your own data
     * POST /api/forex/predict
     *
     * Body example:
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
        log.info("🔮 Prediction request: {}", forexData.getPair());
        PredictionResult result = wekaModelService.predict(forexData);
        return ResponseEntity.ok(result);
    }

    /**
     * 🎲 Prediction with simulated data
     * GET /api/forex/predict/simulate/{pair}
     *
     * Example: GET /api/forex/predict/simulate/EURUSD
     */
    @GetMapping("/predict/simulate/{pair}")
    public ResponseEntity<Map<String, Object>> predictSimulated(
            @PathVariable String pair) {

        log.info("🎲 Simulation request: {}", pair);

        ForexData data = forexDataService.generateSimulatedData(pair);
        PredictionResult result = wekaModelService.predict(data);

        Map<String, Object> response = new HashMap<>();
        response.put("input", data);
        response.put("prediction", result);

        return ResponseEntity.ok(response);
    }

    /**
     * 📊 Scenario tests
     * GET /api/forex/test/scenarios/{pair}
     *
     * Tests 3 different scenarios: BUY, SELL, HOLD
     */
    @GetMapping("/test/scenarios/{pair}")
    public ResponseEntity<Map<String, Object>> testScenarios(
            @PathVariable String pair) {

        log.info("📊 Scenario tests: {}", pair);

        // Oversold → BUY expected
        ForexData oversold = forexDataService.generateOversoldData(pair);
        PredictionResult buyResult = wekaModelService.predict(oversold);

        // Overbought → SELL expected
        ForexData overbought = forexDataService.generateOverboughtData(pair);
        PredictionResult sellResult = wekaModelService.predict(overbought);

        // Neutral → HOLD expected
        ForexData neutral = forexDataService.generateNeutralData(pair);
        PredictionResult holdResult = wekaModelService.predict(neutral);

        Map<String, Object> scenarios = new HashMap<>();

        // BUY scenario
        Map<String, Object> buyScenario = new HashMap<>();
        buyScenario.put("scenario", "Oversold (RSI: 28.3) → BUY expected");
        buyScenario.put("input", oversold);
        buyScenario.put("result", buyResult);
        scenarios.put("buy_scenario", buyScenario);

        // SELL scenario
        Map<String, Object> sellScenario = new HashMap<>();
        sellScenario.put("scenario", "Overbought (RSI: 76.5) → SELL expected");
        sellScenario.put("input", overbought);
        sellScenario.put("result", sellResult);
        scenarios.put("sell_scenario", sellScenario);

        // HOLD scenario
        Map<String, Object> holdScenario = new HashMap<>();
        holdScenario.put("scenario", "Neutral (RSI: 51.2) → HOLD expected");
        holdScenario.put("input", neutral);
        holdScenario.put("result", holdResult);
        scenarios.put("hold_scenario", holdScenario);

        return ResponseEntity.ok(scenarios);
    }

    /**
     * 📈 Predict for multiple currency pairs at once
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
     * 🔄 Retrain the model
     * POST /api/forex/model/retrain
     */
    @PostMapping("/model/retrain")
    public ResponseEntity<Map<String, String>> retrainModel() {
        log.info("🔄 Retraining model...");
        wekaModelService.trainWithSampleData();

        Map<String, String> response = new HashMap<>();
        response.put("status", "✅ Model retrained");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * ℹ️ API info
     * GET /api/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Forex AI Bot");
        info.put("version", "1.0.0");
        info.put("technology", "Spring Boot + Weka ML");
        info.put("supported_pairs", List.of("EURUSD", "GBPUSD", "USDJPY", "AUDUSD", "USDCHF"));

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET  /api/forex/health", "API health");
        endpoints.put("POST /api/forex/predict", "Predict with your own data");
        endpoints.put("GET  /api/forex/predict/simulate/{pair}", "Predict with simulation");
        endpoints.put("GET  /api/forex/test/scenarios/{pair}", "3 scenario test");
        endpoints.put("GET  /api/forex/predict/all", "Predict for all pairs");
        endpoints.put("POST /api/forex/model/retrain", "Retrain the model");
        info.put("endpoints", endpoints);

        return ResponseEntity.ok(info);
    }
}
