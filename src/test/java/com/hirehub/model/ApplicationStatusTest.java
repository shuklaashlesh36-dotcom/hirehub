package com.hirehub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationStatusTest {

    @Test
    void values_matchApplicationWorkflow() {
        assertEquals(ApplicationStatus.APPLIED, ApplicationStatus.valueOf("APPLIED"));
        assertEquals(ApplicationStatus.SHORTLISTED, ApplicationStatus.valueOf("SHORTLISTED"));
        assertEquals(ApplicationStatus.REJECTED, ApplicationStatus.valueOf("REJECTED"));
        assertEquals(ApplicationStatus.HIRED, ApplicationStatus.valueOf("HIRED"));
        assertEquals(4, ApplicationStatus.values().length);
    }
}
