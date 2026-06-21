package com.supersys.analysis.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "resource_allocation")
public class ResourceAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resourceName;
    private String role;
    private Integer hoursPerWeek;
    private Double costPerHour;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private ProjectEntity project;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Integer hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }

    public Double getCostPerHour() { return costPerHour; }
    public void setCostPerHour(Double costPerHour) { this.costPerHour = costPerHour; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public ProjectEntity getProject() { return project; }
    public void setProject(ProjectEntity project) { this.project = project; }
}
