package com.hirehub.service;

import com.hirehub.model.Application;

import java.util.List;
import java.util.Set;

/**
 * Service managing job applications, eligibility validation, and duplicate prevention.
 */
public interface ApplicationService {

    /**
     * Submits a job application for an authenticated candidate.
     *
     * @param seekerId candidate user ID
     * @param jobId    job ID to apply for
     * @return created Application record
     */
    Application applyForJob(Long seekerId, Long jobId);

    /**
     * Retrieves all applications submitted by a candidate.
     *
     * @param seekerId candidate user ID
     * @return list of applications with job and employer details
     */
    List<Application> getCandidateApplications(Long seekerId);

    /**
     * Checks if a candidate has already applied for a specific job.
     *
     * @param seekerId candidate user ID
     * @param jobId    job ID
     * @return true if candidate has already applied
     */
    boolean hasCandidateApplied(Long seekerId, Long jobId);

    /**
     * Returns a set of Job IDs that the candidate has already applied for.
     *
     * @param seekerId candidate user ID
     * @return set of job IDs
     */
    Set<Long> getAppliedJobIds(Long seekerId);
}
