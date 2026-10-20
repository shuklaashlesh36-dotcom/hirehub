package com.hirehub.service;

import com.hirehub.dao.JobDao;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobServiceTest {

    private InMemoryJobDao inMemoryJobDao;
    private JobService jobService;

    @BeforeEach
    void setUp() {
        inMemoryJobDao = new InMemoryJobDao();
        jobService = new JobServiceImpl(inMemoryJobDao);

        Job job1 = new Job(1L, 10L, "Senior Java Engineer", "Develop distributed backend microservices",
                "Java, Spring, MySQL", "Bengaluru", "₹20,00,000", JobStatus.APPROVED);
        job1.setCompanyName("Acme Tech");
        job1.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Job job2 = new Job(2L, 10L, "Full Stack Developer", "Build responsive UI and REST APIs",
                "React, Node.js, TypeScript", "Hyderabad", "₹15,00,000", JobStatus.APPROVED);
        job2.setCompanyName("Acme Tech");
        job2.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Job job3 = new Job(3L, 20L, "DevOps Specialist", "Kubernetes cluster administration",
                "Docker, Kubernetes, AWS", "Pune", "₹18,00,000", JobStatus.PENDING);
        job3.setCompanyName("CloudCorp");
        job3.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Job job4 = new Job(4L, 20L, "Java Software Intern", "Work with senior mentors on core Java projects",
                "Java, OOP, Data Structures", "Bengaluru", "₹5,00,000", JobStatus.APPROVED);
        job4.setCompanyName("CloudCorp");
        job4.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        inMemoryJobDao.save(job1);
        inMemoryJobDao.save(job2);
        inMemoryJobDao.save(job3);
        inMemoryJobDao.save(job4);
    }

    @Test
    void getAvailableJobs_returnsOnlyApprovedJobs() {
        List<Job> available = jobService.getAvailableJobs();
        assertEquals(3, available.size());
        assertTrue(available.stream().allMatch(j -> j.getStatus() == JobStatus.APPROVED));
    }

    @Test
    void searchJobs_byKeyword_matchesTitleSkillsOrCompany() {
        List<Job> javaJobs = jobService.searchJobs("Java", null, "APPROVED");
        assertEquals(2, javaJobs.size());
        assertTrue(javaJobs.stream().anyMatch(j -> j.getTitle().contains("Java")));

        List<Job> cloudJobs = jobService.searchJobs("CloudCorp", null, "ALL");
        assertEquals(2, cloudJobs.size());
    }

    @Test
    void searchJobs_byLocation_filtersAccurately() {
        List<Job> blrJobs = jobService.searchJobs(null, "Bengaluru", "APPROVED");
        assertEquals(2, blrJobs.size());
        assertTrue(blrJobs.stream().allMatch(j -> j.getLocation().equalsIgnoreCase("Bengaluru")));

        List<Job> hydJobs = jobService.searchJobs(null, "Hyderabad", "APPROVED");
        assertEquals(1, hydJobs.size());
    }

    @Test
    void searchJobs_statusAll_includesPendingJobs() {
        List<Job> allJobs = jobService.searchJobs(null, null, "ALL");
        assertEquals(4, allJobs.size());
        assertTrue(allJobs.stream().anyMatch(j -> j.getStatus() == JobStatus.PENDING));
    }

    @Test
    void getJobById_returnsCorrectJobWhenFound() {
        Optional<Job> found = jobService.getJobById(1L);
        assertTrue(found.isPresent());
        assertEquals("Senior Java Engineer", found.get().getTitle());

        Optional<Job> missing = jobService.getJobById(999L);
        assertFalse(missing.isPresent());
    }

    @Test
    void getLocations_returnsDistinctLocations() {
        List<String> locations = jobService.getLocations();
        assertTrue(locations.contains("Bengaluru"));
        assertTrue(locations.contains("Hyderabad"));
        assertTrue(locations.contains("Pune"));
    }

    private static class InMemoryJobDao implements JobDao {
        private final List<Job> jobs = new ArrayList<>();

        @Override
        public Optional<Job> findById(Long id) {
            return jobs.stream().filter(j -> j.getId().equals(id)).findFirst();
        }

        @Override
        public List<Job> findAll() {
            return new ArrayList<>(jobs);
        }

        @Override
        public List<Job> findByStatus(JobStatus status) {
            return jobs.stream().filter(j -> j.getStatus() == status).collect(Collectors.toList());
        }

        @Override
        public List<Job> searchJobs(String keyword, String location, JobStatus status) {
            return jobs.stream().filter(j -> {
                if (status != null && j.getStatus() != status) {
                    return false;
                }
                if (location != null && !location.isBlank()) {
                    if (j.getLocation() == null || !j.getLocation().toLowerCase().contains(location.toLowerCase())) {
                        return false;
                    }
                }
                if (keyword != null && !keyword.isBlank()) {
                    String kw = keyword.toLowerCase();
                    boolean matchTitle = j.getTitle() != null && j.getTitle().toLowerCase().contains(kw);
                    boolean matchDesc = j.getDescription() != null && j.getDescription().toLowerCase().contains(kw);
                    boolean matchSkills = j.getSkills() != null && j.getSkills().toLowerCase().contains(kw);
                    boolean matchComp = j.getCompanyName() != null && j.getCompanyName().toLowerCase().contains(kw);
                    if (!matchTitle && !matchDesc && !matchSkills && !matchComp) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
        }

        @Override
        public List<Job> findByEmployerId(Long employerId) {
            return jobs.stream().filter(j -> employerId.equals(j.getEmployerId())).collect(Collectors.toList());
        }

        @Override
        public List<String> findDistinctLocations() {
            return jobs.stream()
                    .map(Job::getLocation)
                    .filter(loc -> loc != null && !loc.isBlank())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }

        @Override
        public Job save(Job entity) {
            jobs.add(entity);
            return entity;
        }

        @Override
        public void update(Job entity) {
            findById(entity.getId()).ifPresent(existing -> {
                int idx = jobs.indexOf(existing);
                jobs.set(idx, entity);
            });
        }

        @Override
        public void delete(Long id) {
            jobs.removeIf(j -> j.getId().equals(id));
        }

        @Override
        public Optional<Job> findByIdAndEmployerId(Long id, Long employerId) {
            return jobs.stream().filter(j -> j.getId().equals(id) && j.getEmployerId().equals(employerId)).findFirst();
        }

        @Override
        public int countByEmployerId(Long employerId) {
            return (int) jobs.stream().filter(j -> j.getEmployerId().equals(employerId)).count();
        }

        @Override
        public int countActiveByEmployerId(Long employerId) {
            return (int) jobs.stream().filter(j -> j.getEmployerId().equals(employerId) && j.getStatus() == JobStatus.APPROVED).count();
        }
    }
}
