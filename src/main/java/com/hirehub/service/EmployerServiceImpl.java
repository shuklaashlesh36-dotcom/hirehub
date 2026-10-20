package com.hirehub.service;

import com.hirehub.dao.ApplicationDao;
import com.hirehub.dao.JdbcApplicationDao;
import com.hirehub.dao.JdbcJobDao;
import com.hirehub.dao.JobDao;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import com.hirehub.model.EmployerDashboardStats;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link EmployerService} with server-side ownership enforcement,
 * parameter validation, and safe transactional DAO integration.
 */
public class EmployerServiceImpl implements EmployerService {

    private final JobDao jobDao;
    private final ApplicationDao applicationDao;

    public EmployerServiceImpl() {
        this(new JdbcJobDao(), new JdbcApplicationDao());
    }

    public EmployerServiceImpl(JobDao jobDao, ApplicationDao applicationDao) {
        this.jobDao = Objects.requireNonNull(jobDao, "jobDao cannot be null");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao cannot be null");
    }

    @Override
    public EmployerDashboardStats getDashboardStats(Long employerId) {
        if (employerId == null) {
            throw new ValidationException("Employer ID is required.");
        }
        int totalJobs = jobDao.countByEmployerId(employerId);
        int activeJobs = jobDao.countActiveByEmployerId(employerId);
        int totalApplicants = applicationDao.countApplicantsByEmployerId(employerId);
        List<Application> recent = applicationDao.findRecentByEmployerId(employerId, 5);

        return new EmployerDashboardStats(totalJobs, activeJobs, totalApplicants, recent);
    }

    @Override
    public List<Job> getEmployerJobs(Long employerId) {
        if (employerId == null) {
            return List.of();
        }
        return jobDao.findByEmployerId(employerId);
    }

    @Override
    public Optional<Job> getEmployerJob(Long employerId, Long jobId) {
        if (employerId == null || jobId == null) {
            return Optional.empty();
        }
        return jobDao.findByIdAndEmployerId(jobId, employerId);
    }

    @Override
    public Job createJob(Long employerId, Job job) {
        if (employerId == null) {
            throw new ValidationException("Employer ID is required.");
        }
        if (job == null) {
            throw new ValidationException("Job details cannot be null.");
        }

        validateJobFields(job);

        job.setEmployerId(employerId);
        if (job.getStatus() == null || job.getStatus() == JobStatus.PENDING) {
            job.setStatus(JobStatus.APPROVED);
        }

        return jobDao.save(job);
    }

    @Override
    public Job updateJob(Long employerId, Job job) {
        if (employerId == null) {
            throw new ValidationException("Employer ID is required.");
        }
        if (job == null || job.getId() == null) {
            throw new ValidationException("Valid Job and Job ID are required for update.");
        }

        Optional<Job> existingOpt = jobDao.findById(job.getId());
        if (existingOpt.isEmpty()) {
            throw new ValidationException("Job not found.");
        }

        Job existing = existingOpt.get();
        if (!existing.getEmployerId().equals(employerId)) {
            throw new SecurityException("You do not have permission to modify this job.");
        }

        validateJobFields(job);

        existing.setTitle(job.getTitle().trim());
        existing.setDescription(job.getDescription().trim());
        existing.setSkills(job.getSkills() != null ? job.getSkills().trim() : null);
        existing.setLocation(job.getLocation() != null ? job.getLocation().trim() : null);
        existing.setSalary(job.getSalary() != null ? job.getSalary().trim() : null);
        if (job.getStatus() != null) {
            existing.setStatus(job.getStatus());
        }

        jobDao.update(existing);
        return existing;
    }

    @Override
    public void updateJobStatus(Long employerId, Long jobId, JobStatus newStatus) {
        if (employerId == null) {
            throw new ValidationException("Employer ID is required.");
        }
        if (jobId == null) {
            throw new ValidationException("Job ID is required.");
        }
        if (newStatus == null) {
            throw new ValidationException("New job status cannot be null.");
        }

        Optional<Job> existingOpt = jobDao.findById(jobId);
        if (existingOpt.isEmpty()) {
            throw new ValidationException("Job not found.");
        }

        Job existing = existingOpt.get();
        if (!existing.getEmployerId().equals(employerId)) {
            throw new SecurityException("You do not have permission to modify this job.");
        }

        existing.setStatus(newStatus);
        jobDao.update(existing);
    }

    @Override
    public List<Application> getEmployerApplications(Long employerId, Long jobIdFilter) {
        if (employerId == null) {
            return List.of();
        }

        if (jobIdFilter != null) {
            // Verify ownership of the filtered job
            Optional<Job> jobOpt = jobDao.findByIdAndEmployerId(jobIdFilter, employerId);
            if (jobOpt.isEmpty()) {
                throw new SecurityException("You do not have permission to view applications for this job.");
            }
            return applicationDao.findByJobIdAndEmployerId(jobIdFilter, employerId);
        }

        return applicationDao.findByEmployerId(employerId);
    }

    @Override
    public Optional<Application> getEmployerApplication(Long employerId, Long applicationId) {
        if (employerId == null || applicationId == null) {
            return Optional.empty();
        }
        return applicationDao.findByIdAndEmployerId(applicationId, employerId);
    }

    @Override
    public void updateApplicationStatus(Long employerId, Long applicationId, ApplicationStatus newStatus) {
        if (employerId == null) {
            throw new ValidationException("Employer ID is required.");
        }
        if (applicationId == null) {
            throw new ValidationException("Application ID is required.");
        }
        if (newStatus == null) {
            throw new ValidationException("New application status cannot be null.");
        }

        Optional<Application> appOpt = applicationDao.findByIdAndEmployerId(applicationId, employerId);
        if (appOpt.isEmpty()) {
            throw new SecurityException("Application not found or you do not have permission to update it.");
        }

        Application app = appOpt.get();
        app.setStatus(newStatus);
        applicationDao.update(app);
    }

    @Override
    public int getJobApplicantCount(Long jobId) {
        if (jobId == null) {
            return 0;
        }
        return applicationDao.countByJobId(jobId);
    }

    private void validateJobFields(Job job) {
        if (job.getTitle() == null || job.getTitle().trim().isBlank()) {
            throw new ValidationException("Job title is required.");
        }
        if (job.getTitle().trim().length() > 150) {
            throw new ValidationException("Job title must not exceed 150 characters.");
        }
        if (job.getDescription() == null || job.getDescription().trim().isBlank()) {
            throw new ValidationException("Job description is required.");
        }
        if (job.getDescription().trim().length() < 10) {
            throw new ValidationException("Job description must be at least 10 characters.");
        }
        if (job.getSkills() != null && job.getSkills().length() > 500) {
            throw new ValidationException("Skills must not exceed 500 characters.");
        }
        if (job.getLocation() != null && job.getLocation().length() > 100) {
            throw new ValidationException("Location must not exceed 100 characters.");
        }
        if (job.getSalary() != null && job.getSalary().length() > 100) {
            throw new ValidationException("Salary must not exceed 100 characters.");
        }
    }
}
