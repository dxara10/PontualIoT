package com.pontualiot.demo.metrics;

import com.pontualiot.demo.config.MetricsConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MetricsIntegrationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private Counter attendanceRecordsCounter;

    @Autowired
    private MetricsConfig metricsConfig;

    @Test
    void shouldIncrementAttendanceCounter() {
        double initialValue = attendanceRecordsCounter.count();
        
        attendanceRecordsCounter.increment();
        
        assertEquals(initialValue + 1, attendanceRecordsCounter.count());
    }

    @Test
    void shouldTrackActiveDevices() {
        metricsConfig.incrementActiveDevices();
        
        Gauge activeDevicesGauge = meterRegistry.find("iot_devices_active").gauge();
        assertNotNull(activeDevicesGauge);
        assertTrue(activeDevicesGauge.value() > 0);
        
        metricsConfig.decrementActiveDevices();
    }

    @Test
    void shouldRegisterMetricsInRegistry() {
        assertNotNull(meterRegistry.find("attendance_records_total").counter());
        assertNotNull(meterRegistry.find("iot_devices_active").gauge());
    }
}