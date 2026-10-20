package com.hirehub.service;

import com.hirehub.model.Job;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for candidate job browsing, search, and retrieval.
 */
public interface JobService {

    /**
     * Retrieves all approved jobs available for candidates.
     *
     * @return list of approved jobs
     */
    List<Job> getAvailableJobs();

    /**
     * Searches jobs by keyword, location, and optional status filter.
     *
     * @param keyword  free-text keyword matching title, description, skills, or company name
     * @param location location filter
     * @param status   status filter (e.g. "APPROVED", "ALL", etc.)
     * @return list of matching jobs
     */
    List<Job> searchJobs(String keyword, String location, String status);

    /**
     * Finds a single job by its ID.
     *
     * @param id job ID
     * @return optional job if found
     */
    Optional<Job> getJobById(Long id);

    /**
     * Returns all distinct locations for available jobs.
     *
     * @return list of locations
     */
    List<String> getLocations();
}
