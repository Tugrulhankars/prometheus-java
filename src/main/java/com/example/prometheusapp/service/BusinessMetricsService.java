package com.example.prometheusapp.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger processedItems = new AtomicInteger(0);
    private final AtomicInteger activeUsers = new AtomicInteger(0);

    public BusinessMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Gauge: Processed items
        Gauge.builder("business.items.processed", processedItems, AtomicInteger::get)
                .description("Number of processed items")
                .register(meterRegistry);

        // Gauge: Active users (simulated)
        Gauge.builder("business.users.active", activeUsers, AtomicInteger::get)
                .description("Number of active users")
                .register(meterRegistry);

        // Custom business metric: Queue size (simulated)
        AtomicInteger queueSize = new AtomicInteger(0);
        Gauge.builder("business.queue.size", queueSize, AtomicInteger::get)
                .description("Current queue size")
                .register(meterRegistry);

        // Simulate active users
        simulateActiveUsers();
    }

    public void recordBusinessEvent(String eventType) {
        meterRegistry.counter("business.events.total", "type", eventType).increment();
    }

    public void incrementProcessedItems() {
        processedItems.incrementAndGet();
    }

    public int getProcessedItems() {
        return processedItems.get();
    }

    private void simulateActiveUsers() {
        // Simulated active users (for demo purposes)
        activeUsers.set(50 + (int)(Math.random() * 50));
    }
}

