package org.example.app.core.internal;

import java.util.ArrayList;
import java.util.List;

public class EventLog {

    private List<String> logs;

    public EventLog() {
        this.logs = new ArrayList<>();
    }

    // Log a new event
    public void logEvent(String event) {
        logs.add(event);
    }

    // Get all logs
    public List<String> getLogs() {
        return logs;
    }
}
