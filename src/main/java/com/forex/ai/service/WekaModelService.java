package com.forex.ai.service;

import com.forex.ai.model.ForexData;
import com.forex.ai.model.PredictionResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.SerializationHelper;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Weka ML Service
 * Trains, saves, and provides predictions from the model.
 */
@Slf4j
@Service
public class WekaModelService {

    private Classifier model;
    private Instances dataStructure;

    private static final String MODEL_PATH = "models/forex_model.model";
    private static final double MIN_CONFIDENCE = 0.65; // 65% minimum confidence

    /**
     * Prepare the model when the application starts.
     */
    @PostConstruct
    public void initialize() {
        log.info("🤖 Initializing Weka AI Model...");

        // Create the data structure
        dataStructure = createDataStructure();

        // Load the model if it exists, otherwise train it.
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            loadModel();
        } else {
            log.info("📊 Model not found, training a new model...");
            trainWithSampleData();
        }
    }

    /**
     * Create the ARFF data structure
     * (Defines the features)
     */
    private Instances createDataStructure() {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // Technical indicators
        attributes.add(new Attribute("rsi"));
        attributes.add(new Attribute("macd"));
        attributes.add(new Attribute("macd_signal"));
        attributes.add(new Attribute("ema_fast"));
        attributes.add(new Attribute("ema_slow"));
        attributes.add(new Attribute("bb_upper"));
        attributes.add(new Attribute("bb_lower"));
        attributes.add(new Attribute("atr"));
        attributes.add(new Attribute("volume"));

        // Class (target variable)
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("BUY");
        classValues.add("SELL");
        classValues.add("HOLD");
        attributes.add(new Attribute("signal", classValues));

        Instances structure = new Instances("ForexData", attributes, 0);
        structure.setClassIndex(structure.numAttributes() - 1);

        return structure;
    }

    /**
     * Train the model with sample data
     * (In a real project, historical data would be used here)
     */
    public void trainWithSampleData() {
        log.info("📈 Training model with sample data...");

        Instances trainingData = new Instances(dataStructure);

        // Create sample training data
        // In a real project, this would come from historical Forex data
        double[][] samples = {
            // rsi,    macd,    sig,    emaF,    emaS,    bbU,     bbL,    atr,   vol,  signal
            // BUY samples (RSI low, price expected to rise)
            {30.5,  -0.0020, -0.0015, 1.0820, 1.0835, 1.0900, 1.0750, 0.0025, 15000},
            {28.3,  -0.0018, -0.0012, 1.0815, 1.0830, 1.0895, 1.0745, 0.0022, 18000},
            {25.7,  -0.0025, -0.0020, 1.0810, 1.0828, 1.0890, 1.0740, 0.0028, 20000},
            {32.1,  -0.0015, -0.0010, 1.0825, 1.0838, 1.0905, 1.0755, 0.0020, 16500},
            {27.8,   0.0005, -0.0002, 1.0830, 1.0840, 1.0910, 1.0760, 0.0018, 22000},
            {29.4,  -0.0010, -0.0008, 1.0818, 1.0832, 1.0898, 1.0748, 0.0023, 17500},
            {26.9,  -0.0022, -0.0017, 1.0812, 1.0826, 1.0892, 1.0742, 0.0026, 19000},
            {31.5,  -0.0012, -0.0009, 1.0822, 1.0836, 1.0902, 1.0752, 0.0021, 15500},

            // SELL samples (RSI high, price expected to fall)
            {72.5,   0.0025,  0.0018, 1.0920, 1.0905, 1.0990, 1.0850, 0.0028, 14000},
            {75.3,   0.0030,  0.0022, 1.0935, 1.0915, 1.1005, 1.0865, 0.0030, 12000},
            {78.9,   0.0035,  0.0028, 1.0950, 1.0925, 1.1020, 1.0880, 0.0033, 11000},
            {70.1,   0.0020,  0.0015, 1.0910, 1.0898, 1.0980, 1.0840, 0.0025, 16000},
            {73.8,   0.0028,  0.0021, 1.0925, 1.0910, 1.0995, 1.0855, 0.0029, 13500},
            {76.2,   0.0032,  0.0025, 1.0940, 1.0920, 1.1010, 1.0870, 0.0031, 11500},
            {71.6,   0.0022,  0.0017, 1.0915, 1.0902, 1.0985, 1.0845, 0.0026, 15000},
            {74.9,   0.0029,  0.0023, 1.0930, 1.0912, 1.1000, 1.0860, 0.0030, 13000},

            // HOLD samples (Neutral zone)
            {50.2,   0.0002,  0.0001, 1.0870, 1.0868, 1.0940, 1.0800, 0.0015,  9000},
            {52.8,  -0.0003,  0.0002, 1.0872, 1.0870, 1.0942, 1.0802, 0.0016,  8500},
            {48.5,   0.0005, -0.0003, 1.0868, 1.0866, 1.0938, 1.0798, 0.0014,  9500},
            {51.3,  -0.0001,  0.0000, 1.0871, 1.0869, 1.0941, 1.0801, 0.0015,  9200},
            {49.7,   0.0003, -0.0001, 1.0869, 1.0867, 1.0939, 1.0799, 0.0015,  8800},
            {53.1,  -0.0004,  0.0003, 1.0873, 1.0871, 1.0943, 1.0803, 0.0016,  9100},
            {47.9,   0.0006, -0.0004, 1.0867, 1.0865, 1.0937, 1.0797, 0.0014,  9700},
            {50.8,   0.0001,  0.0001, 1.0870, 1.0868, 1.0940, 1.0800, 0.0015,  9000},
        };

        String[] labels = {
            "BUY","BUY","BUY","BUY","BUY","BUY","BUY","BUY",
            "SELL","SELL","SELL","SELL","SELL","SELL","SELL","SELL",
            "HOLD","HOLD","HOLD","HOLD","HOLD","HOLD","HOLD","HOLD"
        };

        // Add instances
        for (int i = 0; i < samples.length; i++) {
            double[] vals = new double[10];
            System.arraycopy(samples[i], 0, vals, 0, 9);
            vals[9] = dataStructure.classAttribute().indexOfValue(labels[i]);

            Instance instance = new DenseInstance(1.0, vals);
            instance.setDataset(trainingData);
            trainingData.add(instance);
        }

        // Train a Random Forest model
        try {
            RandomForest rf = new RandomForest();
            rf.setNumIterations(100);
            rf.setMaxDepth(8);
            rf.setSeed(42);

            rf.buildClassifier(trainingData);
            this.model = rf;

            // Evaluate model accuracy
            evaluateModel(rf, trainingData);

            // Save the model
            saveModel(rf);

            log.info("✅ Model successfully trained!");

        } catch (Exception e) {
            log.error("❌ Error during model training: {}", e.getMessage());
        }
    }

    /**
     * Evaluate model accuracy
     */
    private void evaluateModel(Classifier clf, Instances data) {
        try {
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(clf, data, 5, new Random(42));

            log.info("📊 === Model Evaluation ===");
            log.info("✅ Accuracy: {}", String.format("%.2f%%", eval.pctCorrect()));
            log.info("📈 Kappa: {}", String.format("%.4f", eval.kappa()));

        } catch (Exception e) {
            log.warn("Error during model evaluation: {}", e.getMessage());
        }
    }

    /**
     * Make a prediction — the main method
     */
    public PredictionResult predict(ForexData data) {
        if (model == null) {
            log.error("Model not loaded!");
            return buildErrorResult(data.getPair());
        }

        try {
            // Create an instance
            double[] vals = new double[10];
            vals[0] = data.getRsi();
            vals[1] = data.getMacd();
            vals[2] = data.getMacdSignal();
            vals[3] = data.getEmaFast();
            vals[4] = data.getEmaSlow();
            vals[5] = data.getBbUpper();
            vals[6] = data.getBbLower();
            vals[7] = data.getAtr();
            vals[8] = data.getVolume();
            vals[9] = Utils.missingValue(); // signal is unknown

            Instance instance = new DenseInstance(1.0, vals);
            instance.setDataset(dataStructure);

            // Get prediction
            double predicted = model.classifyInstance(instance);
            double[] probs = model.distributionForInstance(instance);

            String signal = dataStructure.classAttribute().value((int) predicted);
            double confidence = probs[(int) predicted] * 100;

            // Determine risk level
            String riskLevel = calculateRiskLevel(data);

            // Should we trade?
            boolean shouldTrade = confidence >= (MIN_CONFIDENCE * 100)
                    && !signal.equals("HOLD")
                    && !riskLevel.equals("HIGH");

            String reason = buildReason(signal, data, confidence);

            log.info("🔮 Prediction: {} | Confidence: {:.1f}% | Pair: {}",
                    signal, confidence, data.getPair());

            return PredictionResult.builder()
                    .signal(signal)
                    .buyProbability(Math.round(probs[0] * 10000.0) / 100.0)
                    .sellProbability(Math.round(probs[1] * 10000.0) / 100.0)
                    .holdProbability(Math.round(probs[2] * 10000.0) / 100.0)
                    .confidence(Math.round(confidence * 100.0) / 100.0)
                    .shouldTrade(shouldTrade)
                    .reason(reason)
                    .pair(data.getPair())
                    .riskLevel(riskLevel)
                    .timestamp(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();

        } catch (Exception e) {
            log.error("Error during prediction: {}", e.getMessage());
            return buildErrorResult(data.getPair());
        }
    }

    /**
     * Calculate risk level
     */
    private String calculateRiskLevel(ForexData data) {
        double rsi = data.getRsi();
        double atr = data.getAtr();

        // Extreme zone: RSI < 20 or > 80
        if (rsi < 20 || rsi > 80) return "HIGH";

        // If ATR is high, volatility is high
        if (atr > 0.0040) return "HIGH";
        if (atr > 0.0025) return "MEDIUM";

        return "LOW";
    }

    /**
     * Write an explanation for the signal
     */
    private String buildReason(String signal, ForexData data, double confidence) {
        StringBuilder sb = new StringBuilder();
        double rsi = data.getRsi();

        switch (signal) {
            case "BUY" -> {
                sb.append("RSI in low zone (").append(String.format("%.1f", rsi)).append(")");
                if (data.getMacd() > data.getMacdSignal())
                    sb.append(", MACD bullish crossover");
                if (data.getClose() < data.getBbLower())
                    sb.append(", Price below lower BB line");
            }
            case "SELL" -> {
                sb.append("RSI in high zone (").append(String.format("%.1f", rsi)).append(")");
                if (data.getMacd() < data.getMacdSignal())
                    sb.append(", MACD bearish crossover");
                if (data.getClose() > data.getBbUpper())
                    sb.append(", Price above upper BB line");
            }
            case "HOLD" ->
                sb.append("Neutral zone, no clear signal (RSI: ")
                  .append(String.format("%.1f", rsi)).append(")");
        }

        sb.append(String.format(" | Confidence: %.1f%%", confidence));
        return sb.toString();
    }

    /**
     * Save the model to a file
     */
    private void saveModel(Classifier clf) {
        try {
            new File("models").mkdirs();
            SerializationHelper.write(MODEL_PATH, clf);
            log.info("💾 Model saved: {}", MODEL_PATH);
        } catch (Exception e) {
            log.error("Error while saving model: {}", e.getMessage());
        }
    }

    /**
     * Load the model from a file
     */
    private void loadModel() {
        try {
            model = (Classifier) SerializationHelper.read(MODEL_PATH);
            log.info("✅ Model loaded: {}", MODEL_PATH);
        } catch (Exception e) {
            log.warn("Model not loaded, retraining...");
            trainWithSampleData();
        }
    }

    /**
     * Standard result in case of error
     */
    private PredictionResult buildErrorResult(String pair) {
        return PredictionResult.builder()
                .signal("HOLD")
                .confidence(0.0)
                .shouldTrade(false)
                .reason("Model error - do not trade!")
                .pair(pair)
                .riskLevel("HIGH")
                .timestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}
