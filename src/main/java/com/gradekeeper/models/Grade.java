package com.gradekeeper.models;

public class Grade {
    private int id;
    private int classId;
    private String assignment;
    private double score;
    private double maxPoints;

    public Grade(int id, int classId, String assignment, double score, double maxPoints) {
        this.id = id;
        this.classId = classId;
        this.assignment = assignment;
        this.score = score;
        this.maxPoints = maxPoints;
    }

    public int getId() { return id; }
    public int getClassId() { return classId; }
    public String getAssignment() { return assignment; }
    public double getScore() { return score; }
    public double getMaxPoints() { return maxPoints; }

    public double getPercentage() {
        return (score / maxPoints) * 100;
    }
}