package com.hirehub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleTest {

    @Test
    void values_includeAllPortalRoles() {
        assertEquals(3, Role.values().length);
        assertNotNull(Role.valueOf("ADMIN"));
        assertNotNull(Role.valueOf("EMPLOYER"));
        assertNotNull(Role.valueOf("JOB_SEEKER"));
    }
}
