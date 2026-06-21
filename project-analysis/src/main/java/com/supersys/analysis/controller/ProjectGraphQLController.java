package com.supersys.analysis.controller;

import com.supersys.analysis.entity.ProjectEntity;
import com.supersys.analysis.entity.ResourceAllocation;
import com.supersys.analysis.entity.ScheduleEntity;
import com.supersys.analysis.repository.ProjectRepository;
import com.supersys.analysis.repository.ScheduleRepository;
import com.supersys.analysis.service.AnalysisRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ProjectGraphQLController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AnalysisRestService analysisService;

    @QueryMapping
    public List<ProjectEntity> findAllProjects() {
        return projectRepository.findAll();
    }

    @QueryMapping
    public ProjectEntity findProjectById(@Argument Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @Transactional
    public ProjectEntity createProject(@Argument Map<String, Object> input) {
        ProjectEntity project = new ProjectEntity();
        populateProject(project, input);
        if (project.getStatus() == null) {
            project.setStatus("CREATED");
        }
        return projectRepository.save(project);
    }

    @MutationMapping
    @Transactional
    public ProjectEntity updateProject(@Argument Long id, @Argument Map<String, Object> input) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        populateProject(project, input);
        return projectRepository.save(project);
    }

    @MutationMapping
    @Transactional
    public Boolean deleteProject(@Argument Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @MutationMapping
    @Transactional
    public Boolean requestProjectAnalysis(@Argument Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        analysisService.requestProjectAnalysis(project);
        project.setStatus("ANALYSIS_PENDING");
        projectRepository.save(project);
        return true;
    }

    @MutationMapping
    @Transactional
    public Boolean requestResourceAnalysis(@Argument Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        analysisService.requestResourceAnalysis(project);
        project.setStatus("RESOURCES_ANALYSIS_PENDING");
        projectRepository.save(project);
        return true;
    }

    private void populateProject(ProjectEntity project, Map<String, Object> input) {
        if (input.containsKey("name")) project.setName((String) input.get("name"));
        if (input.containsKey("description")) project.setDescription((String) input.get("description"));
        if (input.containsKey("budget") && input.get("budget") != null) {
            Number budget = (Number) input.get("budget");
            project.setBudget(budget.doubleValue());
        }
        if (input.containsKey("status")) project.setStatus((String) input.get("status"));

        if (input.containsKey("scheduleId") && input.get("scheduleId") != null) {
            Long scheduleId = Long.parseLong(input.get("scheduleId").toString());
            ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            project.setSchedule(schedule);
        }

        if (input.containsKey("allocations") && input.get("allocations") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocsInput = (List<Map<String, Object>>) input.get("allocations");
            List<ResourceAllocation> allocations = new ArrayList<>();
            for (Map<String, Object> allocMap : allocsInput) {
                ResourceAllocation alloc = new ResourceAllocation();
                alloc.setResourceName((String) allocMap.get("resourceName"));
                alloc.setRole((String) allocMap.get("role"));
                if (allocMap.containsKey("hoursPerWeek")) alloc.setHoursPerWeek((Integer) allocMap.get("hoursPerWeek"));
                if (allocMap.containsKey("costPerHour") && allocMap.get("costPerHour") != null) {
                    Number cost = (Number) allocMap.get("costPerHour");
                    alloc.setCostPerHour(cost.doubleValue());
                }
                if (allocMap.containsKey("quantity")) alloc.setQuantity((Integer) allocMap.get("quantity"));
                alloc.setProject(project);
                allocations.add(alloc);
            }
            project.getAllocations().clear();
            project.getAllocations().addAll(allocations);
        }
    }
}
