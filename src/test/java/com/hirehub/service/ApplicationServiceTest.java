package com.hirehub.service;

import com.hirehub.dao.ApplicationDao;
import com.hirehub.dao.JobDao;
import com.hirehub.dao.ResumeDao;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;
import com.hirehub.model.Resume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTest {

    private InMemoryApplicationDao applicationDao;
    private InMemoryJobDao jobDao;
    private InMemoryResumeDao resumeDao;
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationDao = new InMemoryApplicationDao();
        jobDao = new InMemoryJobDao();
        resumeDao = new InMemoryResumeDao();
        applicationService = new ApplicationServiceImpl(applicationDao, jobDao, resumeDao);

        // Seed an approved job
        Job approvedJob = new Job(100L, 5L, "Backend Java Lead", "Design systems", "Java, SQL", "Bengaluru", "₹25 LPA", JobStatus.APPROVED);
        jobDao.save(approvedJob);

        // Seed a pending job
        Job pendingJob = new Job(200L, 5L, "Junior Tester", "Run QA", "Testing", "Remote", "₹5 LPA", JobStatus.PENDING);
        jobDao.save(pendingJob);

        // Seed a candidate resume for seeker 1
        Resume resume = new Resume(50L, 1L, "candidate_cv.pdf", "/uploads/candidate_cv.pdf");
        resumeDao.save(resume);
    }

    @Test
    void applyForJob_successWithResumeAttached() {
        Application app = applicationService.applyForJob(1L, 100L);

        assertNotNull(app);
        assertEquals(100L, app.getJobId());
        assertEquals(1L, app.getSeekerId());
        assertEquals(50L, app.getResumeId(), "Candidate's resume ID should be attached.");
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());
    }

    @Test
    void applyForJob_successWithoutResume() {
        // Seeker 2 has no resume
        Application app = applicationService.applyForJob(2L, 100L);

        assertNotNull(app);
        assertEquals(100L, app.getJobId());
        assertEquals(2L, app.getSeekerId());
        assertNull(app.getResumeId(), "Resume ID should be null if candidate has no resume.");
    }

    @Test
    void applyForJob_preventsDuplicateApplication() {
        applicationService.applyForJob(1L, 100L);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                applicationService.applyForJob(1L, 100L)
        );
        assertTrue(ex.getMessage().contains("already submitted an application"));
    }

    @Test
    void applyForJob_rejectsNonExistentJob() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                applicationService.applyForJob(1L, 9999L)
        );
        assertTrue(ex.getMessage().contains("no longer exists"));
    }

    @Test
    void applyForJob_rejectsNonApprovedJob() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                applicationService.applyForJob(1L, 200L)
        );
        assertTrue(ex.getMessage().contains("not currently accepting applications"));
    }

    @Test
    void getCandidateApplications_returnsOnlyCandidatesApplications() {
        applicationService.applyForJob(1L, 100L);
        applicationService.applyForJob(2L, 100L);

        List<Application> seeker1Apps = applicationService.getCandidateApplications(1L);
        assertEquals(1, seeker1Apps.size());
        assertEquals(1L, seeker1Apps.get(0).getSeekerId());

        List<Application> seeker2Apps = applicationService.getCandidateApplications(2L);
        assertEquals(1, seeker2Apps.size());
        assertEquals(2L, seeker2Apps.get(0).getSeekerId());
    }

    @Test
    void hasCandidateApplied_and_getAppliedJobIds_workCorrectly() {
        assertFalse(applicationService.hasCandidateApplied(1L, 100L));
        Set<Long> before = applicationService.getAppliedJobIds(1L);
        assertFalse(before.contains(100L));

        applicationService.applyForJob(1L, 100L);

        assertTrue(applicationService.hasCandidateApplied(1L, 100L));
        Set<Long> after = applicationService.getAppliedJobIds(1L);
        assertTrue(after.contains(100L));
    }

    private static class InMemoryApplicationDao implements ApplicationDao {
        private final List<Application> apps = new ArrayList<>();
        private long idGen = 1;

        @Override
        public List<Application> findBySeekerId(Long seekerId) {
            return apps.stream().filter(a -> a.getSeekerId().equals(seekerId)).collect(Collectors.toList());
        }

        @Override
        public boolean existsByJobIdAndSeekerId(Long jobId, Long seekerId) {
            return apps.stream().anyMatch(a -> a.getJobId().equals(jobId) && a.getSeekerId().equals(seekerId));
        }

        @Override
        public List<Application> findByJobId(Long jobId) {
            return apps.stream().filter(a -> a.getJobId().equals(jobId)).collect(Collectors.toList());
        }

        @Override
        public Optional<Application> findById(Long id) {
            return apps.stream().filter(a -> a.getId().equals(id)).findFirst();
        }

        @Override
        public List<Application> findAll() {
            return new ArrayList<>(apps);
        }

        @Override
        public Application save(Application entity) {
            entity.setId(idGen++);
            apps.add(entity);
            return entity;
        }

        @Override
        public void update(Application entity) {
            findById(entity.getId()).ifPresent(e -> {
                int idx = apps.indexOf(e);
                apps.set(idx, entity);
            });
        }

        @Override
        public void delete(Long id) {
            apps.removeIf(a -> a.getId().equals(id));
        }

        @Override public List<Application> findByEmployerId(Long empId) { return List.of(); }
        @Override public List<Application> findByJobIdAndEmployerId(Long jId, Long empId) { return List.of(); }
        @Override public Optional<Application> findByIdAndEmployerId(Long aId, Long empId) { return Optional.empty(); }
        @Override public int countApplicantsByEmployerId(Long empId) { return 0; }
        @Override public List<Application> findRecentByEmployerId(Long empId, int limit) { return List.of(); }
        @Override public int countByJobId(Long jobId) { return 0; }
    }

    private static class InMemoryJobDao implements JobDao {
        private final List<Job> jobs = new ArrayList<>();

        @Override public Optional<Job> findById(Long id) {
            return jobs.stream().filter(j -> j.getId().equals(id)).findFirst();
        }
        @Override public List<Job> findAll() { return new ArrayList<>(jobs); }
        @Override public List<Job> findByStatus(JobStatus s) { return jobs.stream().filter(j -> j.getStatus() == s).collect(Collectors.toList()); }
        @Override public List<Job> searchJobs(String kw, String loc, JobStatus s) { return jobs; }
        @Override public List<Job> findByEmployerId(Long empId) { return List.of(); }
        @Override public List<String> findDistinctLocations() { return List.of(); }
        @Override public Optional<Job> findByIdAndEmployerId(Long id, Long empId) {
            return jobs.stream().filter(j -> j.getId().equals(id) && j.getEmployerId().equals(empId)).findFirst();
        }
        @Override public int countByEmployerId(Long empId) { return (int) jobs.stream().filter(j -> j.getEmployerId().equals(empId)).count(); }
        @Override public int countActiveByEmployerId(Long empId) { return (int) jobs.stream().filter(j -> j.getEmployerId().equals(empId) && j.getStatus() == JobStatus.APPROVED).count(); }
        @Override public Job save(Job entity) { jobs.add(entity); return entity; }
        @Override public void update(Job entity) {}
        @Override public void delete(Long id) {}
    }

    private static class InMemoryResumeDao implements ResumeDao {
        private final List<Resume> resumes = new ArrayList<>();

        @Override public Optional<Resume> findLatestBySeekerId(Long seekerId) {
            return resumes.stream().filter(r -> r.getSeekerId().equals(seekerId)).findFirst();
        }
        @Override public Optional<Resume> findById(Long id) { return resumes.stream().filter(r -> r.getId().equals(id)).findFirst(); }
        @Override public List<Resume> findAll() { return new ArrayList<>(resumes); }
        @Override public Resume save(Resume entity) { resumes.add(entity); return entity; }
        @Override public void update(Resume entity) {}
        @Override public void delete(Long id) {}
    }
}
