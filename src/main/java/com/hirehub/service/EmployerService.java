package com.hirehub.service;

import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import com.hirehub.model.EmployerDashboardStats;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Employer Dashboard, Job Management, and Applicant Review operations.
 */
public interface EmployerService {

    /**
     * Aggregates real-time dashboard metrics and recent applicants for an employer.
     *
     * @param employerId employer user ID
     * @return populated dashboard stats
     */
    EmployerDashboardStats getDashboardStats(Long employerId);

    /**
     * Lists all jobs posted by the employer.
     *
     * @param employerId employer user ID
     * @return list of jobs
     */
    List<Job> getEmployerJobs(Long employerId);

    /**
     * Retrieves a single job strictly ensuring ownership by the employer.
     *
     * @param employerId employer user ID
     * @param jobId      job ID
     * @return optional job if owned
     */
    Optional<Job> getEmployerJob(Long employerId, Long jobId);

    /**
     * Validates and creates a new job listing for the employer.
     *
     * @param employerId employer user ID
     * @param job        job entity to create
     * @return created job with assigned ID
     */
    Job createJob(Long employerId, Job job);

    /**
     * Validates and updates an existing job listing, enforcing server-side ownership.
     *
     * @param employerId employer user ID
     * @param job        updated job entity
     * @return updated job
     */
    Job updateJob(Long employerId, Job job);

    /**
     * Changes job status (e.g. APPROVED or CLOSED), enforcing server-side ownership.
     *
     * @param employerId employer user ID
     * @param jobId      job ID
     * @param newStatus  target status
     */
    void updateJobStatus(Long employerId, Long jobId, JobStatus newStatus);

    /**
     * Retrieves applicants across all jobs or for a specific job, enforcing ownership.
     *
     * @param employerId  employer user ID
     * @param jobIdFilter optional job ID filter (null for all jobs)
     * @return list of candidate applications with contact details
     */
    List<Application> getEmployerApplications(Long employerId, Long jobIdFilter);

    /**
     * Retrieves a single application strictly verifying it belongs to a job owned by the employer.
     *
     * @param employerId    employer user ID
     * @param applicationId application ID
     * @return optional application if owned
     */
    Optional<Application> getEmployerApplication(Long employerId, Long applicationId);

    /**
     * Updates an applicant's status (e.g. SHORTLISTED, HIRED, REJECTED), enforcing ownership.
     *
     * @param employerId    employer user ID
     * @param applicationId application ID
     * @param newStatus     new application status
     */
    void updateApplicationStatus(Long employerId, Long applicationId, ApplicationStatus newStatus);

    /**
     * Returns total applicants count for a given job.
     *
     * @param jobId job ID
     * @return count of applications
     */
    int getJobApplicantCount(Long jobId);
}
