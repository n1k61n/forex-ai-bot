# 🤖 Forex AI Bot — Spring Boot + Weka

**Java Spring Boot** və **Weka ML** istifadə edərək hazırlanmış Forex trading siqnal sistemi.

---

## 🗂️ Layihə Strukturu

```
forex-ai-bot/
├── src/
│   ├── main/java/com/forex/ai/
│   │   ├── ForexAiApplication.java          ← Başlanğıc nöqtəsi
│   │   ├── controller/
│   │   │   └── ForexController.java          ← REST API endpoint-lər
│   │   ├── service/
│   │   │   ├── WekaModelService.java         ← AI/ML məntiqi
│   │   │   └── ForexDataService.java         ← Data simulyasiyası
│   │   └── model/
│   │       ├── ForexData.java                ← Giriş modeli
│   │       └── PredictionResult.java         ← Çıxış modeli
│   └── test/java/com/forex/ai/
│       └── ForexAiApplicationTests.java      ← Unit testlər
├── pom.xml                                   ← Maven asılılıqları
└── README.md
```

---

## ⚡ Quraşdırma və İşə Salma

### Tələblər
- Java 17+
- Maven 3.8+

### Addımlar

```bash
# 1. Klonla
git clone <repo-url>
cd forex-ai-bot

# 2. Qur
mvn clean install

# 3. İşə sal
mvn spring-boot:run

# 4. Testləri çalışdır
mvn test
```

---

## 🌐 API Endpoint-lər

| Method | URL | Açıqlama |
|--------|-----|----------|
| GET  | `/api/forex/health` | API sağlamlığı |
| GET  | `/api/forex/info` | Bütün endpoint-lər |
| POST | `/api/forex/predict` | Öz datanla proqnoz |
| GET  | `/api/forex/predict/simulate/{pair}` | Simulyasiya ilə proqnoz |
| GET  | `/api/forex/test/scenarios/{pair}` | 3 ssenari testi |
| GET  | `/api/forex/predict/all` | Bütün cütlər |
| POST | `/api/forex/model/retrain` | Modeli yenilə |

---

## 📡 API İstifadəsi

### 1. Öz Datanla Proqnoz

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

**Cavab:**
```json
{
  "signal": "BUY",
  "buyProbability": 78.5,
  "sellProbability": 12.3,
  "holdProbability": 9.2,
  "confidence": 78.5,
  "shouldTrade": true,
  "reason": "RSI aşağı zona (30.5), MACD bullish kəsişmə | Əminlik: 78.5%",
  "pair": "EURUSD",
  "riskLevel": "LOW",
  "timestamp": "2024-01-15 14:30:22"
}
```

### 2. Simulyasiya ilə Test

```bash
curl http://localhost:8080/api/forex/predict/simulate/EURUSD
```

### 3. Ssenari Testləri

```bash
curl http://localhost:8080/api/forex/test/scenarios/EURUSD
```

### 4. Bütün Valyuta Cütləri

```bash
curl http://localhost:8080/api/forex/predict/all
```

---

## 🧠 AI Modeli

### İstifadə Olunan Texnologiya
- **Algoritm:** Random Forest (100 ağac)
- **Kitabxana:** Weka 3.8.6
- **Siqnallar:** BUY / SELL / HOLD

### Feature-lər (Giriş Məlumatları)

| Feature | Açıqlama |
|---------|----------|
| RSI | Relative Strength Index (0-100) |
| MACD | Moving Average Convergence Divergence |
| MACD Signal | MACD siqnal xətti |
| EMA Fast | 12-periodik EMA |
| EMA Slow | 26-periodik EMA |
| BB Upper | Bollinger Band yuxarı xətti |
| BB Lower | Bollinger Band aşağı xətti |
| ATR | Average True Range (volatilite) |
| Volume | İşlem həcmi |

### Qərar Məntiqi

```
RSI < 30  → BUY zonası
RSI > 70  → SELL zonası
RSI 40-60 → HOLD zonası

Əminlik < 65% → Trade etmə
Risk HIGH    → Trade etmə
```

---

## 🧪 Testlər

```bash
# Bütün testlər
mvn test

# Test çıxışı nümunəsi:
# ✅ Oversold Testi: RSI=28.3 → BUY (78.5%)
# ✅ Overbought Testi: RSI=76.5 → SELL (82.1%)
# ✅ Neytral Testi: RSI=51.2 → HOLD (71.3%)
# ✅ EURUSD: BUY (68.2%)
# ✅ GBPUSD: SELL (74.5%)
# ✅ USDJPY: HOLD (69.8%)
# ✅ AUDUSD: BUY (71.2%)
```

---

## 🔧 Real JForex İnteqrasiyası

Real trading üçün `ForexDataService`-i JForex API ilə əvəzləyin:

```java
// ForexDataService.java-da bu metodu dəyişdirin:
public ForexData getMarketData(String pair) {
    // JForex API-dan real məlumat alın
    IBar bar = context.getHistory().getBar(
        Instrument.valueOf(pair), Period.ONE_HOUR, OfferSide.ASK, 0
    );

    double rsi = indicators.rsi(instrument, period, AppliedPrice.CLOSE, 14, 1);
    // ... digər indikatorlar

    return ForexData.builder()
        .pair(pair)
        .close(bar.getClose())
        .rsi(rsi)
        // ...
        .build();
}
```

---

## ⚠️ Xəbərdarlıq

> Bu proyekt **tədris məqsədlidir**. Real pul ilə trade etməzdən əvvəl:
> - Demo hesabda test edin
> - Risk menecmenti tətbiq edin
> - Mütəxəssis məsləhəti alın
