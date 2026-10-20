package com.hirehub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSubclassTest {

    @Test
    void admin_setsRoleAndHidesPasswordInToString() {
        Admin admin = new Admin(1L, "Site Admin", "admin@hirehub.com", "secretPass");
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("ADMIN_DASHBOARD", admin.getDashboardType());
        assertFalse(admin.toString().contains("secretPass"));
        assertTrue(admin.toString().contains("admin@hirehub.com"));
    }

    @Test
    void employer_keepsCompanyFields() {
        Employer employer = new Employer(2L, "Priya", "priya@co.com", "secretPass",
                "Tech Corp", "IT services");
        assertEquals(Role.EMPLOYER, employer.getRole());
        assertEquals("Tech Corp", employer.getCompanyName());
        assertEquals("EMPLOYER_DASHBOARD", employer.getDashboardType());
        assertFalse(employer.toString().contains("secretPass"));
    }

    @Test
    void jobSeeker_keepsProfileFields() {
        JobSeeker seeker = new JobSeeker(3L, "Rahul", "rahul@mail.com", "secretPass",
                "9876543210", "Java", "B.Tech");
        assertEquals(Role.JOB_SEEKER, seeker.getRole());
        assertEquals("Java", seeker.getSkills());
        assertEquals("JOB_SEEKER_DASHBOARD", seeker.getDashboardType());
        assertFalse(seeker.toString().contains("secretPass"));
    }
}
