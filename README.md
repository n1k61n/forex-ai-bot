# 🤖 Forex AI Bot — Spring Boot + Weka

A Forex trading signal system built with **Java Spring Boot** and **Weka ML**.

---

## 🗂️ Project Structure

```
forex-ai-bot/
├── src/
│   ├── main/java/com/forex/ai/
│   │   ├── ForexAiApplication.java          ← Entry point
│   │   ├── controller/
│   │   │   └── ForexController.java          ← REST API endpoints
│   │   ├── service/
│   │   │   ├── WekaModelService.java         ← AI/ML logic
│   │   │   └── ForexDataService.java         ← Data simulation
│   │   └── model/
│   │       ├── ForexData.java                ← Input model
│   │       └── PredictionResult.java         ← Output model
│   └── test/java/com/forex/ai/
│       └── ForexAiApplicationTests.java      ← Unit tests
├── pom.xml                                   ← Maven dependencies
└── README.md
```

---

## ⚡ Setup & Running

### Requirements
- Java 17+
- Maven 3.8+

### Steps

```bash
# 1. Clone
git clone <repo-url>
cd forex-ai-bot

# 2. Build
mvn clean install

# 3. Run
mvn spring-boot:run

# 4. Run tests
mvn test
```

---

## 🌐 API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET  | `/api/forex/health` | API health check |
| GET  | `/api/forex/info` | All endpoints info |
| POST | `/api/forex/predict` | Predict with your own data |
| GET  | `/api/forex/predict/simulate/{pair}` | Predict with simulated data |
| GET  | `/api/forex/test/scenarios/{pair}` | Run 3 scenario tests |
| GET  | `/api/forex/predict/all` | Predict for all pairs |
| POST | `/api/forex/model/retrain` | Retrain the model |

---

## 📡 API Usage

### 1. Predict with Your Own Data

```bash
curl -X POST http://localhost:8080/api/forex/predict \
  -H "Content-Type: application/json" \
  -d '{
    "pair": "EURUSD",
    "rsi": 30.5,
    "macd": -0.0020,
    "macdSignal": -0.0015,
    "emaFast": 1.0820,
    "emaSlow": 1.0835,
    "bbUpper": 1.0900,
    "bbLower": 1.0750,
    "atr": 0.0025,
    "close": 1.0810,
    "volume": 15000
  }'
```

**Response:**
```json
{
  "signal": "BUY",
  "buyProbability": 78.5,
  "sellProbability": 12.3,
  "holdProbability": 9.2,
  "confidence": 78.5,
  "shouldTrade": true,
  "reason": "RSI in low zone (30.5), MACD bullish crossover | Confidence: 78.5%",
  "pair": "EURUSD",
  "riskLevel": "LOW",
  "timestamp": "2024-01-15 14:30:22"
}
```

### 2. Simulate & Test

```bash
curl http://localhost:8080/api/forex/predict/simulate/EURUSD
```

### 3. Scenario Tests

```bash
curl http://localhost:8080/api/forex/test/scenarios/EURUSD
```

### 4. All Currency Pairs

```bash
curl http://localhost:8080/api/forex/predict/all
```

---

## 🧠 AI Model

### Technologies Used
- **Algorithm:** Random Forest (100 trees)
- **Library:** Weka 3.8.6
- **Signals:** BUY / SELL / HOLD

### Features (Input Data)

| Feature | Description |
|---------|-------------|
| RSI | Relative Strength Index (0-100) |
| MACD | Moving Average Convergence Divergence |
| MACD Signal | MACD signal line |
| EMA Fast | 12-period EMA |
| EMA Slow | 26-period EMA |
| BB Upper | Bollinger Band upper band |
| BB Lower | Bollinger Band lower band |
| ATR | Average True Range (volatility) |
| Volume | Trading volume |

### Decision Logic

```
RSI < 30  → BUY zone
RSI > 70  → SELL zone
RSI 40-60 → HOLD zone

Confidence < 65% → Do not trade
Risk HIGH        → Do not trade
```

---

## 🧪 Tests

```bash
# Run all tests
mvn test

# Sample test output:
# ✅ Oversold Test:  RSI=28.3 → BUY  (78.5%)
# ✅ Overbought Test: RSI=76.5 → SELL (82.1%)
# ✅ Neutral Test:   RSI=51.2 → HOLD (71.3%)
# ✅ EURUSD: BUY  (68.2%)
# ✅ GBPUSD: SELL (74.5%)
# ✅ USDJPY: HOLD (69.8%)
# ✅ AUDUSD: BUY  (71.2%)
```

---

## 🔧 Real JForex Integration

For live trading, replace `ForexDataService` with JForex API calls:

```java
// Update this method in ForexDataService.java:
public ForexData getMarketData(String pair) {
    // Fetch real data from JForex API
    IBar bar = context.getHistory().getBar(
        Instrument.valueOf(pair), Period.ONE_HOUR, OfferSide.ASK, 0
    );

    double rsi = indicators.rsi(instrument, period, AppliedPrice.CLOSE, 14, 1);
    // ... other indicators

    return ForexData.builder()
        .pair(pair)
        .close(bar.getClose())
        .rsi(rsi)
        // ...
        .build();
}
```

---

## ⚠️ Disclaimer

> This project is intended for **educational purposes only**. Before trading with real money:
> - Test thoroughly on a demo account
> - Apply proper risk management
> - Seek advice from a financial professional
