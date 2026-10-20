package com.hirehub.service;

import com.hirehub.dao.ApplicationDao;
import com.hirehub.dao.JdbcApplicationDao;
import com.hirehub.dao.JdbcJobDao;
import com.hirehub.dao.JdbcResumeDao;
import com.hirehub.dao.JobDao;
import com.hirehub.dao.ResumeDao;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;
import com.hirehub.model.Resume;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ApplicationService} with job status verification,
 * duplicate application checks, and resume association.
 */
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationDao applicationDao;
    private final JobDao jobDao;
    private final ResumeDao resumeDao;

    public ApplicationServiceImpl() {
        this(new JdbcApplicationDao(), new JdbcJobDao(), new JdbcResumeDao());
    }

    public ApplicationServiceImpl(ApplicationDao applicationDao, JobDao jobDao, ResumeDao resumeDao) {
        this.applicationDao = applicationDao;
        this.jobDao = jobDao;
        this.resumeDao = resumeDao;
    }

    @Override
    public Application applyForJob(Long seekerId, Long jobId) {
        if (seekerId == null) {
            throw new ValidationException("Candidate authentication is required to apply for jobs.");
        }
        if (jobId == null) {
            throw new ValidationException("Please specify a valid job to apply for.");
        }

        // 1. Verify Job exists and is active/approved
        Optional<Job> jobOpt = jobDao.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new ValidationException("The selected job posting no longer exists.");
        }

        Job job = jobOpt.get();
        if (job.getStatus() != JobStatus.APPROVED) {
            throw new ValidationException("This job listing is not currently accepting applications (Status: " + job.getStatus() + ").");
        }

        // 2. Prevent duplicate application
        if (applicationDao.existsByJobIdAndSeekerId(jobId, seekerId)) {
            throw new ValidationException("You have already submitted an application for '" + job.getTitle() + "'.");
        }

        // 3. Link candidate's latest resume if available
        Optional<Resume> resumeOpt = resumeDao.findLatestBySeekerId(seekerId);
        Long resumeId = resumeOpt.map(Resume::getId).orElse(null);

        // 4. Save application
        Application app = new Application(jobId, seekerId, resumeId, ApplicationStatus.APPLIED);
        return applicationDao.save(app);
    }

    @Override
    public List<Application> getCandidateApplications(Long seekerId) {
        if (seekerId == null) {
            return List.of();
        }
        return applicationDao.findBySeekerId(seekerId);
    }

    @Override
    public boolean hasCandidateApplied(Long seekerId, Long jobId) {
        if (seekerId == null || jobId == null) {
            return false;
        }
        return applicationDao.existsByJobIdAndSeekerId(jobId, seekerId);
    }

    @Override
    public Set<Long> getAppliedJobIds(Long seekerId) {
        if (seekerId == null) {
            return Set.of();
        }
        return applicationDao.findBySeekerId(seekerId).stream()
                .map(Application::getJobId)
                .collect(Collectors.toSet());
    }
}
