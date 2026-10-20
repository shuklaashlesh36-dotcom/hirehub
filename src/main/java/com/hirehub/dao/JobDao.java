package com.hirehub.dao;

import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;

import java.util.List;

/**
 * Persistence contract for Job listings.
 */
public interface JobDao extends GenericDao<Job, Long> {

    /**
     * Finds jobs filtered by approval status.
     *
     * @param status job status to match
     * @return list of matching jobs
     */
    List<Job> findByStatus(JobStatus status);

    /**
     * Searches jobs with keyword matching, location filtering, and optional status filtering.
     *
     * @param keyword  search keyword matching title, description, skills, or company name
     * @param location location filter
     * @param status   job status filter, or null for default/any
     * @return matching jobs
     */
    List<Job> searchJobs(String keyword, String location, JobStatus status);

    /**
     * Finds all jobs posted by a specific employer.
     *
     * @param employerId employer user ID
     * @return list of jobs
     */
    List<Job> findByEmployerId(Long employerId);

    /**
     * Returns a list of distinct locations where jobs are posted.
     *
     * @return distinct locations
     */
    List<String> findDistinctLocations();

    /**
     * Finds a single job ensuring it belongs to the given employer.
     *
     * @param id         job ID
     * @param employerId employer user ID
     * @return optional job if matched and owned
     */
    java.util.Optional<Job> findByIdAndEmployerId(Long id, Long employerId);

    /**
     * Counts total jobs posted by an employer.
     *
     * @param employerId employer user ID
     * @return count of jobs
     */
    int countByEmployerId(Long employerId);

    /**
     * Counts active (APPROVED) jobs posted by an employer.
     *
     * @param employerId employer user ID
     * @return count of active jobs
     */
    int countActiveByEmployerId(Long employerId);
}
