package com.hirehub.dao;

import com.hirehub.model.Application;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for candidate job applications.
 */
public interface ApplicationDao extends GenericDao<Application, Long> {

    /**
     * Finds all applications submitted by a specific candidate.
     *
     * @param seekerId candidate user ID
     * @return list of applications with joined job and employer details
     */
    List<Application> findBySeekerId(Long seekerId);

    /**
     * Checks if a candidate has already applied for a specific job.
     *
     * @param jobId    job ID
     * @param seekerId candidate user ID
     * @return true if an application already exists
     */
    boolean existsByJobIdAndSeekerId(Long jobId, Long seekerId);

    /**
     * Finds all applications submitted for a given job (for employer view).
     *
     * @param jobId job ID
     * @return list of applications
     */
    List<Application> findByJobId(Long jobId);

    /**
     * Finds all applications for jobs posted by a specific employer.
     *
     * @param employerId employer user ID
     * @return list of applications with candidate details
     */
    List<Application> findByEmployerId(Long employerId);

    /**
     * Finds applications for a specific job owned by a specific employer.
     *
     * @param jobId      job ID
     * @param employerId employer user ID
     * @return list of applications
     */
    List<Application> findByJobIdAndEmployerId(Long jobId, Long employerId);

    /**
     * Finds a single application ensuring it belongs to a job posted by the employer.
     *
     * @param applicationId application ID
     * @param employerId    employer user ID
     * @return optional application if matched and owned
     */
    Optional<Application> findByIdAndEmployerId(Long applicationId, Long employerId);

    /**
     * Counts total applicants across all jobs posted by the employer.
     *
     * @param employerId employer user ID
     * @return count of applications
     */
    int countApplicantsByEmployerId(Long employerId);

    /**
     * Returns recent applications across all jobs posted by the employer.
     *
     * @param employerId employer user ID
     * @param limit      max records to return
     * @return list of recent applications
     */
    List<Application> findRecentByEmployerId(Long employerId, int limit);

    /**
     * Counts applicants for a specific job.
     *
     * @param jobId job ID
     * @return count of applications
     */
    int countByJobId(Long jobId);
}
