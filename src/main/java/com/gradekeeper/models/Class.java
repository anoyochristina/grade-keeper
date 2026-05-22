package com.gradekeeper.models;

public class Class {
    private int id;
    private String name;
    private String createdAt;

    public Class(int id, String name, String createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCreatedAt() { return createdAt; }
    public void setName(String name) { this.name = name; }
}