package com.hirehub.util;

import com.hirehub.model.Admin;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
import com.hirehub.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRoleDemoTest {

    @Test
    void userReference_canHoldEachSubclass() {
        User admin = new Admin("A", "a@hirehub.com", "password1");
        User employer = new Employer("E", "e@co.com", "password1", "Co", "Desc");
        User seeker = new JobSeeker("S", "s@mail.com", "password1", "1", "Java", "B.Tech");

        assertEquals("ADMIN_DASHBOARD", UserRoleDemo.dashboardTypeOf(admin));
        assertEquals("EMPLOYER_DASHBOARD", UserRoleDemo.dashboardTypeOf(employer));
        assertEquals("JOB_SEEKER_DASHBOARD", UserRoleDemo.dashboardTypeOf(seeker));
    }

    @Test
    void sampleUsers_areStoredInGenericList() {
        List<User> users = UserRoleDemo.sampleUsers();
        List<String> types = UserRoleDemo.dashboardTypes(users);

        assertEquals(3, users.size());
        assertEquals(3, types.size());
        assertTrue(types.contains("ADMIN_DASHBOARD"));
        assertTrue(types.contains("EMPLOYER_DASHBOARD"));
        assertTrue(types.contains("JOB_SEEKER_DASHBOARD"));
    }
}
