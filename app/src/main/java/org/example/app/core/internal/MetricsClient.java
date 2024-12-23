package core.internal;

import java.util.Map;

public class MetricsClient {

    private Metrics metrics;

    public MetricsClient(Metrics metrics) {
        this.metrics = metrics;
    }

    // Send the collected metrics to an external monitoring service
    public void sendMetrics() {
        // This would typically involve HTTP requests or pushing data to an external service.
        Map<String, Long> allMetrics = metrics.getAllMetrics();
        System.out.println("Sending metrics: " + allMetrics);
        // Logic to send metrics to external service goes here
    }
}
