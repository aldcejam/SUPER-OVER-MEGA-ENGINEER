package com.supersys.analysis.controller;

import com.supersys.analysis.entity.ScheduleEntity;
import com.supersys.analysis.entity.ScheduleStep;
import com.supersys.analysis.repository.ScheduleRepository;
import com.supersys.analysis.service.AnalysisRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class ScheduleGraphQLController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AnalysisRestService analysisService;

    @QueryMapping
    public List<ScheduleEntity> findAllSchedules() {
        return scheduleRepository.findAll();
    }

    @QueryMapping
    public ScheduleEntity findScheduleById(@Argument Long id) {
        return scheduleRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @Transactional
    public ScheduleEntity createSchedule(@Argument Map<String, Object> input) {
        ScheduleEntity entity = new ScheduleEntity();
        populateEntity(entity, input);
        if (entity.getStatus() == null) {
            entity.setStatus("CREATED");
        }
        return scheduleRepository.save(entity);
    }

    @MutationMapping
    @Transactional
    public ScheduleEntity updateSchedule(@Argument Long id, @Argument Map<String, Object> input) {
        ScheduleEntity entity = scheduleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Schedule not found"));
        populateEntity(entity, input);
        return scheduleRepository.save(entity);
    }

    @MutationMapping
    @Transactional
    public Boolean deleteSchedule(@Argument Long id) {
        if (scheduleRepository.existsById(id)) {
            scheduleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @MutationMapping
    public Boolean requestAnalysis(@Argument Long id) {
        ScheduleEntity entity = scheduleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Schedule not found"));
        analysisService.requestScheduleAnalysis(entity);
        entity.setStatus("ANALYSIS_PENDING");
        scheduleRepository.save(entity);
        return true;
    }


    private void populateEntity(ScheduleEntity entity, Map<String, Object> input) {
        if (input.containsKey("title")) entity.setTitle((String) input.get("title"));
        if (input.containsKey("status")) entity.setStatus((String) input.get("status"));
        if (input.containsKey("details")) entity.setDetails((String) input.get("details"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        if (input.containsKey("startDate") && input.get("startDate") != null) {
            entity.setStartDate(LocalDate.parse((String) input.get("startDate"), formatter));
        }
        if (input.containsKey("endDate") && input.get("endDate") != null) {
            entity.setEndDate(LocalDate.parse((String) input.get("endDate"), formatter));
        }

        if (input.containsKey("steps") && input.get("steps") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepsInput = (List<Map<String, Object>>) input.get("steps");
            List<ScheduleStep> steps = stepsInput.stream().map(stepMap -> {
                ScheduleStep step = new ScheduleStep();
                step.setStepName((String) stepMap.get("stepName"));
                step.setDescription((String) stepMap.get("description"));
                step.setSequence((Integer) stepMap.get("sequence"));
                step.setDaysRequired((Integer) stepMap.get("daysRequired"));
                step.setCompleted((Boolean) stepMap.get("completed"));
                return step;
            }).toList();
            entity.setSteps(steps);
        }
    }
}
