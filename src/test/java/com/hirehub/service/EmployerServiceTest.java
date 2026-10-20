package com.hirehub.service;

import com.hirehub.dao.ApplicationDao;
import com.hirehub.dao.JobDao;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import com.hirehub.model.EmployerDashboardStats;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployerServiceTest {

    private InMemoryJobDao jobDao;
    private InMemoryApplicationDao applicationDao;
    private EmployerService employerService;

    private static final Long EMPLOYER_1 = 10L;
    private static final Long EMPLOYER_2 = 20L;

    @BeforeEach
    void setUp() {
        jobDao = new InMemoryJobDao();
        applicationDao = new InMemoryApplicationDao();
        employerService = new EmployerServiceImpl(jobDao, applicationDao);

        // Seed jobs for Employer 1
        Job job1 = new Job(1L, EMPLOYER_1, "Full Stack Java Dev", "Develop scalable backend services", "Java, MySQL", "Bengaluru", "₹15 LPA", JobStatus.APPROVED);
        Job job2 = new Job(2L, EMPLOYER_1, "DevOps Engineer", "Manage Kubernetes and CI/CD pipelines", "AWS, Docker", "Remote", "₹18 LPA", JobStatus.CLOSED);
        jobDao.save(job1);
        jobDao.save(job2);

        // Seed job for Employer 2
        Job job3 = new Job(3L, EMPLOYER_2, "React Developer", "Build responsive UI web apps", "React, TypeScript", "Hyderabad", "₹12 LPA", JobStatus.APPROVED);
        jobDao.save(job3);

        // Seed applications for Job 1 (Employer 1)
        Application app1 = new Application(101L, 1L, 1001L, 501L, ApplicationStatus.APPLIED, new java.sql.Timestamp(System.currentTimeMillis()));
        app1.setJobTitle("Full Stack Java Dev");
        app1.setSeekerName("Alice Seeker");
        app1.setSeekerEmail("alice@example.com");
        applicationDao.save(app1);

        // Seed application for Job 3 (Employer 2)
        Application app2 = new Application(102L, 3L, 1002L, 502L, ApplicationStatus.APPLIED, new java.sql.Timestamp(System.currentTimeMillis()));
        app2.setJobTitle("React Developer");
        app2.setSeekerName("Bob Seeker");
        app2.setSeekerEmail("bob@example.com");
        applicationDao.save(app2);
    }

    @Test
    void getDashboardStats_aggregatesMetricsCorrectly() {
        EmployerDashboardStats stats = employerService.getDashboardStats(EMPLOYER_1);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalJobs());
        assertEquals(1, stats.getActiveJobs());
        assertEquals(1, stats.getTotalApplicants());
        assertEquals(1, stats.getRecentApplications().size());
        assertEquals("Alice Seeker", stats.getRecentApplications().get(0).getSeekerName());
    }

    @Test
    void createJob_success_persistsWithEmployerIdAndApprovedStatus() {
        Job newJob = new Job();
        newJob.setTitle("Cloud Architect");
        newJob.setDescription("Design enterprise cloud migration architecture");
        newJob.setLocation("Remote");
        newJob.setSalary("₹30 LPA");
        newJob.setSkills("GCP, Terraform");

        Job created = employerService.createJob(EMPLOYER_1, newJob);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(EMPLOYER_1, created.getEmployerId());
        assertEquals(JobStatus.APPROVED, created.getStatus());
        assertEquals("Cloud Architect", created.getTitle());
    }

    @Test
    void createJob_validationErrors_missingTitle_shortDescription() {
        Job invalidJob1 = new Job();
        invalidJob1.setTitle("");
        invalidJob1.setDescription("Valid description with enough characters");

        assertThrows(ValidationException.class, () -> employerService.createJob(EMPLOYER_1, invalidJob1));

        Job invalidJob2 = new Job();
        invalidJob2.setTitle("Good Title");
        invalidJob2.setDescription("Short");

        assertThrows(ValidationException.class, () -> employerService.createJob(EMPLOYER_1, invalidJob2));
    }

    @Test
    void updateJob_success_updatesFields() {
        Job updateDetails = new Job();
        updateDetails.setId(1L);
        updateDetails.setTitle("Lead Java & Spring Dev");
        updateDetails.setDescription("Updated description with enough characters");
        updateDetails.setLocation("Bengaluru, Hybrid");
        updateDetails.setSalary("₹20 LPA");
        updateDetails.setSkills("Java 17, Spring");
        updateDetails.setStatus(JobStatus.APPROVED);

        Job updated = employerService.updateJob(EMPLOYER_1, updateDetails);

        assertEquals("Lead Java & Spring Dev", updated.getTitle());
        assertEquals("Bengaluru, Hybrid", updated.getLocation());
    }

    @Test
    void updateJob_unauthorizedCrossEmployer_throwsSecurityException() {
        // Employer 1 attempting to edit Job 3 which belongs to Employer 2
        Job unauthorizedEdit = new Job();
        unauthorizedEdit.setId(3L);
        unauthorizedEdit.setTitle("Malicious Title Edit");
        unauthorizedEdit.setDescription("Description with sufficient length");

        SecurityException ex = assertThrows(SecurityException.class, () ->
                employerService.updateJob(EMPLOYER_1, unauthorizedEdit));
        assertTrue(ex.getMessage().contains("permission"));
    }

    @Test
    void updateJobStatus_closeAndReopen_success() {
        employerService.updateJobStatus(EMPLOYER_1, 1L, JobStatus.CLOSED);
        assertEquals(JobStatus.CLOSED, jobDao.findById(1L).get().getStatus());

        employerService.updateJobStatus(EMPLOYER_1, 1L, JobStatus.APPROVED);
        assertEquals(JobStatus.APPROVED, jobDao.findById(1L).get().getStatus());
    }

    @Test
    void updateJobStatus_unauthorizedCrossEmployer_throwsSecurityException() {
        // Employer 1 attempting to close Job 3 owned by Employer 2
        assertThrows(SecurityException.class, () ->
                employerService.updateJobStatus(EMPLOYER_1, 3L, JobStatus.CLOSED));
    }

    @Test
    void getEmployerApplications_filteredByJob_unauthorizedCrossEmployer_throwsSecurityException() {
        // Employer 1 attempting to list applicants for Job 3 owned by Employer 2
        assertThrows(SecurityException.class, () ->
                employerService.getEmployerApplications(EMPLOYER_1, 3L));
    }

    @Test
    void updateApplicationStatus_success_updatesHiringStage() {
        employerService.updateApplicationStatus(EMPLOYER_1, 101L, ApplicationStatus.SHORTLISTED);
        Application updated = applicationDao.findById(101L).get();
        assertEquals(ApplicationStatus.SHORTLISTED, updated.getStatus());

        employerService.updateApplicationStatus(EMPLOYER_1, 101L, ApplicationStatus.HIRED);
        assertEquals(ApplicationStatus.HIRED, applicationDao.findById(101L).get().getStatus());
    }

    @Test
    void updateApplicationStatus_unauthorizedCrossEmployer_throwsSecurityException() {
        // Employer 1 attempting to update status of Application 102 (Job 3, owned by Employer 2)
        assertThrows(SecurityException.class, () ->
                employerService.updateApplicationStatus(EMPLOYER_1, 102L, ApplicationStatus.REJECTED));
    }

    // In-Memory Test DAOs
    private static class InMemoryJobDao implements JobDao {
        private final List<Job> jobs = new ArrayList<>();
        private long idGen = 100;

        @Override public Optional<Job> findById(Long id) {
            return jobs.stream().filter(j -> j.getId().equals(id)).findFirst();
        }
        @Override public List<Job> findAll() { return new ArrayList<>(jobs); }
        @Override public List<Job> findByStatus(JobStatus s) { return jobs.stream().filter(j -> j.getStatus() == s).collect(Collectors.toList()); }
        @Override public List<Job> searchJobs(String kw, String loc, JobStatus s) { return jobs; }
        @Override public List<Job> findByEmployerId(Long empId) {
            return jobs.stream().filter(j -> j.getEmployerId().equals(empId)).collect(Collectors.toList());
        }
        @Override public List<String> findDistinctLocations() { return List.of(); }
        @Override public Optional<Job> findByIdAndEmployerId(Long id, Long empId) {
            return jobs.stream().filter(j -> j.getId().equals(id) && j.getEmployerId().equals(empId)).findFirst();
        }
        @Override public int countByEmployerId(Long empId) {
            return (int) jobs.stream().filter(j -> j.getEmployerId().equals(empId)).count();
        }
        @Override public int countActiveByEmployerId(Long empId) {
            return (int) jobs.stream().filter(j -> j.getEmployerId().equals(empId) && j.getStatus() == JobStatus.APPROVED).count();
        }
        @Override public Job save(Job entity) {
            if (entity.getId() == null) {
                entity.setId(idGen++);
            }
            jobs.add(entity);
            return entity;
        }
        @Override public void update(Job entity) {
            findById(entity.getId()).ifPresent(e -> {
                int idx = jobs.indexOf(e);
                jobs.set(idx, entity);
            });
        }
        @Override public void delete(Long id) { jobs.removeIf(j -> j.getId().equals(id)); }
    }

    private static class InMemoryApplicationDao implements ApplicationDao {
        private final List<Application> apps = new ArrayList<>();
        private long idGen = 200;

        @Override public List<Application> findBySeekerId(Long seekerId) { return List.of(); }
        @Override public boolean existsByJobIdAndSeekerId(Long jobId, Long seekerId) { return false; }
        @Override public List<Application> findByJobId(Long jobId) {
            return apps.stream().filter(a -> a.getJobId().equals(jobId)).collect(Collectors.toList());
        }
        @Override public List<Application> findByEmployerId(Long employerId) {
            return apps.stream().filter(a -> a.getId().equals(101L)).collect(Collectors.toList());
        }
        @Override public List<Application> findByJobIdAndEmployerId(Long jobId, Long employerId) {
            return apps.stream().filter(a -> a.getJobId().equals(jobId)).collect(Collectors.toList());
        }
        @Override public Optional<Application> findByIdAndEmployerId(Long applicationId, Long employerId) {
            return apps.stream().filter(a -> a.getId().equals(applicationId) &&
                    ((employerId.equals(EMPLOYER_1) && a.getId().equals(101L)) ||
                     (employerId.equals(EMPLOYER_2) && a.getId().equals(102L)))).findFirst();
        }
        @Override public int countApplicantsByEmployerId(Long employerId) {
            return (int) apps.stream().filter(a -> employerId.equals(EMPLOYER_1) && a.getId().equals(101L)).count();
        }
        @Override public List<Application> findRecentByEmployerId(Long employerId, int limit) {
            return apps.stream().filter(a -> employerId.equals(EMPLOYER_1) && a.getId().equals(101L)).limit(limit).collect(Collectors.toList());
        }
        @Override public int countByJobId(Long jobId) {
            return (int) apps.stream().filter(a -> a.getJobId().equals(jobId)).count();
        }
        @Override public Optional<Application> findById(Long id) {
            return apps.stream().filter(a -> a.getId().equals(id)).findFirst();
        }
        @Override public List<Application> findAll() { return new ArrayList<>(apps); }
        @Override public Application save(Application entity) {
            if (entity.getId() == null) {
                entity.setId(idGen++);
            }
            apps.add(entity);
            return entity;
        }
        @Override public void update(Application entity) {
            findById(entity.getId()).ifPresent(e -> {
                int idx = apps.indexOf(e);
                apps.set(idx, entity);
            });
        }
        @Override public void delete(Long id) { apps.removeIf(a -> a.getId().equals(id)); }
    }
}
