package com.hirehub.service;

import com.hirehub.dao.JdbcJobDao;
import com.hirehub.dao.JobDao;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link JobService} handling candidate job queries and filter resolution.
 */
public class JobServiceImpl implements JobService {

    private final JobDao jobDao;

    public JobServiceImpl() {
        this(new JdbcJobDao());
    }

    public JobServiceImpl(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    @Override
    public List<Job> getAvailableJobs() {
        return jobDao.findByStatus(JobStatus.APPROVED);
    }

    @Override
    public List<Job> searchJobs(String keyword, String location, String status) {
        JobStatus jobStatus;

        if (status != null && !status.isBlank()) {
            String trimmed = status.trim();
            if ("ALL".equalsIgnoreCase(trimmed)) {
                jobStatus = null;
            } else {
                try {
                    jobStatus = JobStatus.valueOf(trimmed.toUpperCase());
                } catch (IllegalArgumentException e) {
                    jobStatus = JobStatus.APPROVED;
                }
            }
        } else {
            // Default for candidates browsing: show APPROVED listings
            jobStatus = JobStatus.APPROVED;
        }

        String sanitizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String sanitizedLocation = (location != null && !location.isBlank()) ? location.trim() : null;

        return jobDao.searchJobs(sanitizedKeyword, sanitizedLocation, jobStatus);
    }

    @Override
    public Optional<Job> getJobById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jobDao.findById(id);
    }

    @Override
    public List<String> getLocations() {
        return jobDao.findDistinctLocations();
    }
}
