package com.example.prometheusapp.controller;

import com.example.prometheusapp.service.BusinessMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class MetricsController {

    private final MeterRegistry meterRegistry;
    private final BusinessMetricsService businessMetricsService;
    private final Timer requestTimer;
    private final Random random = new Random();

    public MetricsController(MeterRegistry meterRegistry, 
                            BusinessMetricsService businessMetricsService) {
        this.meterRegistry = meterRegistry;
        this.businessMetricsService = businessMetricsService;
        this.requestTimer = Timer.builder("api.request.duration")
                .description("API request duration in seconds")
                .register(meterRegistry);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        meterRegistry.counter("api.requests.total", 
                "version", "v1",
                "endpoint", "health", 
                "status", "success").increment();
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "spring-boot-prometheus-app");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData() {
        try {
            return requestTimer.recordCallable(() -> {
                meterRegistry.counter("api.requests.total", 
                        "version", "v1",
                        "endpoint", "data", 
                        "status", "success").increment();
                
                // Simüle edilmiş iş mantığı
                long delay = random.nextInt(100) + 10; // 10-110ms
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted", e);
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Data retrieved successfully");
                response.put("timestamp", System.currentTimeMillis());
                
                businessMetricsService.recordBusinessEvent("data.retrieved");
                
                return ResponseEntity.ok(response);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processData(@RequestBody Map<String, Object> data) {
        try {
            return requestTimer.recordCallable(() -> {
                try {
                    meterRegistry.counter("api.requests.total", 
                            "version", "v1",
                            "endpoint", "process", 
                            "status", "success").increment();
                    
                    // İş mantığı simülasyonu
                    long delay = random.nextInt(200) + 50;
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted", e);
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Data processed successfully");
                    response.put("processed", true);
                    
                    businessMetricsService.recordBusinessEvent("data.processed");
                    businessMetricsService.incrementProcessedItems();
                    
                    return ResponseEntity.ok(response);
                } catch (Exception e) {
                    meterRegistry.counter("api.requests.total", 
                            "version", "v1",
                            "endpoint", "process", 
                            "status", "error").increment();
                    meterRegistry.counter("api.errors.total", 
                        "type", "processing_error",
                        "exception", e.getClass().getSimpleName()).increment();
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/slow")
    public ResponseEntity<Map<String, String>> slowEndpoint() {
        try {
            return requestTimer.recordCallable(() -> {
                meterRegistry.counter("api.requests.total", 
                        "version", "v1",
                        "endpoint", "slow", 
                        "status", "success").increment();
                
                // Yavaş endpoint simülasyonu (alert tetiklemek için)
                long delay = random.nextInt(2000) + 1000; // 1-3 saniye
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted", e);
                }
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Slow operation completed");
                return ResponseEntity.ok(response);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

