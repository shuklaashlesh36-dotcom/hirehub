package com.hirehub.model;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobTest {

    @Test
    void defaultConstructor_setsDefaultStatusToPending() {
        Job job = new Job();
        assertEquals(JobStatus.PENDING, job.getStatus());
        assertEquals("Full-time", job.getJobType());
    }

    @Test
    void fullConstructor_populatesAllFields() {
        Job job = new Job(
                10L, 2L, "Senior Java Developer", "Build enterprise APIs",
                "Java, Spring, MySQL", "Bengaluru", "₹18,00,000 - ₹24,00,000",
                JobStatus.APPROVED
        );

        assertEquals(10L, job.getId());
        assertEquals(2L, job.getEmployerId());
        assertEquals("Senior Java Developer", job.getTitle());
        assertEquals("Build enterprise APIs", job.getDescription());
        assertEquals("Java, Spring, MySQL", job.getSkills());
        assertEquals("Bengaluru", job.getLocation());
        assertEquals("₹18,00,000 - ₹24,00,000", job.getSalary());
        assertEquals(JobStatus.APPROVED, job.getStatus());
    }

    @Test
    void settersAndDisplayFields_workAsExpected() {
        Job job = new Job();
        job.setId(101L);
        job.setEmployerId(5L);
        job.setTitle("Frontend Architect");
        job.setCompanyName("Google India");
        job.setJobType("Remote");
        job.setLocation("Hyderabad");
        job.setSalary("Competitive");
        job.setStatus(JobStatus.APPROVED);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        job.setCreatedAt(now);

        assertEquals("Google India", job.getCompanyName());
        assertEquals("Remote", job.getJobType());
        assertEquals("Hyderabad", job.getLocation());
        assertEquals(now, job.getCreatedAt());
        assertTrue(job.toString().contains("Google India"));
    }
}
