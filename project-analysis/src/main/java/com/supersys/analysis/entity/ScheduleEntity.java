package com.supersys.analysis.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(columnDefinition = "TEXT")
    private String analysisResult;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ScheduleStep> steps;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(String analysisResult) { this.analysisResult = analysisResult; }

    public List<ScheduleStep> getSteps() { return steps; }
    public void setSteps(List<ScheduleStep> steps) { this.steps = steps; }
}
