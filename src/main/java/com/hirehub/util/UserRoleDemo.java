package com.hirehub.util;

import com.hirehub.model.Admin;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
import com.hirehub.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows polymorphism: a {@code User} reference can hold Admin, Employer, or JobSeeker.
 * Later phases can reuse {@link #dashboardTypeOf(User)} when routing after login.
 */
public final class UserRoleDemo {

    private UserRoleDemo() {
    }

    public static String dashboardTypeOf(User user) {
        return user.getDashboardType();
    }

    public static List<User> sampleUsers() {
        List<User> users = new ArrayList<>();
        users.add(new Admin(1L, "Site Admin", "admin@hirehub.com", "ChangeMe@123"));
        users.add(new Employer(2L, "Priya Shah", "priya@company.com", "ChangeMe@123",
                "Tech Corp", "Software services"));
        users.add(new JobSeeker(3L, "Rahul Mehta", "rahul@mail.com", "ChangeMe@123",
                "9876543210", "Java, SQL", "B.Tech"));
        return users;
    }

    public static List<String> dashboardTypes(List<User> users) {
        List<String> types = new ArrayList<>();
        for (User user : users) {
            types.add(dashboardTypeOf(user));
        }
        return types;
    }
}
