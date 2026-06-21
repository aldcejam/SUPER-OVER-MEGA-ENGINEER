package com.supersys.analysis.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double budget;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String analysisResult;

    @Column(columnDefinition = "TEXT")
    private String resourceAnalysisResult;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "schedule_id")
    private ScheduleEntity schedule;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ResourceAllocation> allocations = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(String analysisResult) { this.analysisResult = analysisResult; }

    public String getResourceAnalysisResult() { return resourceAnalysisResult; }
    public void setResourceAnalysisResult(String resourceAnalysisResult) { this.resourceAnalysisResult = resourceAnalysisResult; }

    public ScheduleEntity getSchedule() { return schedule; }
    public void setSchedule(ScheduleEntity schedule) { this.schedule = schedule; }

    public List<ResourceAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<ResourceAllocation> allocations) { 
        this.allocations = allocations; 
        if (allocations != null) {
            for (ResourceAllocation alloc : allocations) {
                alloc.setProject(this);
            }
        }
    }
}
