package com.hirehub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobStatusTest {

    @Test
    void values_matchModerationWorkflow() {
        assertEquals(JobStatus.PENDING, JobStatus.valueOf("PENDING"));
        assertEquals(JobStatus.APPROVED, JobStatus.valueOf("APPROVED"));
        assertEquals(JobStatus.REJECTED, JobStatus.valueOf("REJECTED"));
        assertEquals(JobStatus.CLOSED, JobStatus.valueOf("CLOSED"));
        assertEquals(4, JobStatus.values().length);
    }
}
