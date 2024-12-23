package core.internal;

import java.util.HashMap;
import java.util.Map;

public class Metrics {

    private Map<String, Long> metrics;

    public Metrics() {
        this.metrics = new HashMap<>();
    }

    // Add a new metric
    public void addMetric(String metricName, long value) {
        metrics.put(metricName, value);
    }

    // Get a metric value
    public long getMetric(String metricName) {
        return metrics.getOrDefault(metricName, 0L);
    }

    // Get all metrics
    public Map<String, Long> getAllMetrics() {
        return metrics;
    }
}
