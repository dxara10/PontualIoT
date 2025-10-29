package com.pontualiot.simulator;

/**
 * Representa um funcionário
 */
public class Employee {
    private Long id;
    private String name;
    private String email;
    private String rfidTag;

    public Employee(String name, String email, String rfidTag) {
        this.name = name;
        this.email = email;
        this.rfidTag = rfidTag;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRfidTag() { return rfidTag; }
}