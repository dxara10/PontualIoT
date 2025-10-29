package com.pontualiot.simulator;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulatorService {
    private final MqttPublisher publisher;
    private final RfidSimulator simulator;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running;
    private final AtomicInteger eventsGenerated;
    private final Random random;

    private final String[] RFID_TAGS = {"TAG001", "TAG002", "TAG003", "TAG004", "TAG005"};
    private final String[] EVENT_TYPES = {"CHECK_IN", "CHECK_OUT"};

    public SimulatorService(String brokerUrl) {
        this.publisher = new MqttPublisher(brokerUrl);
        this.simulator = new RfidSimulator("RFID-SIM-001");
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = new AtomicBoolean(false);
        this.eventsGenerated = new AtomicInteger(0);
        this.random = new Random();
    }

    public boolean startSimulation() {
        if (running.get()) return true;
        
        if (!publisher.connect()) return false;
        
        running.set(true);
        scheduler.scheduleAtFixedRate(this::generateRandomEvent, 0, 2, TimeUnit.SECONDS);
        return true;
    }

    public void stopSimulation() {
        running.set(false);
        scheduler.shutdown();
        publisher.disconnect();
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getEventsGenerated() {
        return eventsGenerated.get();
    }

    private void generateRandomEvent() {
        if (!running.get()) return;
        
        String rfidTag = RFID_TAGS[random.nextInt(RFID_TAGS.length)];
        String eventType = EVENT_TYPES[random.nextInt(EVENT_TYPES.length)];
        
        AttendanceEvent event = new AttendanceEvent(
            simulator.getDeviceId(), rfidTag, eventType
        );
        
        if (publisher.publishAttendanceEvent(event)) {
            eventsGenerated.incrementAndGet();
        }
    }
}