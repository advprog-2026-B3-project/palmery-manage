package id.ac.ui.cs.advprog.palmerymanage.event;

import java.time.Instant;
import java.util.Map;

public class DomainEventMessage {

    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private int attempts;
    private Map<String, Object> payload;

    public DomainEventMessage() {
        // Jackson constructor
    }

    public DomainEventMessage(String eventId, String eventType, Instant occurredAt, int attempts, Map<String, Object> payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.attempts = attempts;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}