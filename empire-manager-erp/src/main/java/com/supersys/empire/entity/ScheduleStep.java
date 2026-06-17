package com.supersys.empire.entity;

import java.io.Serializable;

public class ScheduleStep implements Serializable {

    private String stepName;
    private String description;
    private Integer sequence;
    private Integer daysRequired;
    private Boolean completed;

    // Default constructor for Jackson
    public ScheduleStep() {}

    public ScheduleStep(String stepName, String description, Integer sequence, Integer daysRequired, Boolean completed) {
        this.stepName = stepName;
        this.description = description;
        this.sequence = sequence;
        this.daysRequired = daysRequired;
        this.completed = completed;
    }

    // Getters and Setters
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public Integer getDaysRequired() { return daysRequired; }
    public void setDaysRequired(Integer daysRequired) { this.daysRequired = daysRequired; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
