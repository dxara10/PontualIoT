package com.pontualiot.simulator;

public class SimulatorApplication {
    public static void main(String[] args) {
        String brokerUrl = args.length > 0 ? args[0] : "tcp://localhost:1883";
        
        SimulatorService service = new SimulatorService(brokerUrl);
        
        System.out.println("🚀 Starting IoT Simulator...");
        System.out.println("📡 MQTT Broker: " + brokerUrl);
        
        if (service.startSimulation()) {
            System.out.println("✅ Simulator started successfully");
            System.out.println("📊 Generating random attendance events every 2 seconds");
            System.out.println("Press Ctrl+C to stop");
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Stopping simulator...");
                service.stopSimulation();
                System.out.println("📈 Total events generated: " + service.getEventsGenerated());
            }));
            
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                service.stopSimulation();
            }
        } else {
            System.err.println("❌ Failed to start simulator - check MQTT broker connection");
            System.exit(1);
        }
    }
}