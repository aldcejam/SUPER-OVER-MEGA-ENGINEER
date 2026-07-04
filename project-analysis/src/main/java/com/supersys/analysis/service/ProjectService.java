package com.supersys.analysis.service;

import com.supersys.analysis.entity.ProjectEntity;
import com.supersys.analysis.repository.ProjectRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Bulkhead(name = "projectQueryBulkhead")
    public ProjectEntity findProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }
}
