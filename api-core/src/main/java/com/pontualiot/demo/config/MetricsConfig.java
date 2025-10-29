package com.pontualiot.demo.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    private final AtomicInteger activeDevices = new AtomicInteger(0);

    @Bean
    public Counter attendanceRecordsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("attendance_records_total")
                .description("Total attendance records processed")
                .register(meterRegistry);
    }

    @Bean
    public Gauge activeDevicesGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("iot_devices_active", activeDevices, AtomicInteger::get)
                .description("Number of active IoT devices")
                .register(meterRegistry);
    }

    public void incrementActiveDevices() {
        activeDevices.incrementAndGet();
    }

    public void decrementActiveDevices() {
        activeDevices.decrementAndGet();
    }
}