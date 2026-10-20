package com.hirehub.service;

import com.hirehub.model.User;

import java.util.Optional;

/**
 * Service handling authentication, registration policies, and user lookup.
 */
public interface AuthService {

    /**
     * Registers a new employer or job seeker.
     * Public registration as ADMIN is strictly disallowed.
     *
     * @param user user entity with role, credentials, and profile data
     * @return the created User with generated ID
     */
    User register(User user);

    /**
     * Authenticates a user with email and password.
     *
     * @param email    user email address
     * @param password raw plain-text password
     * @return the authenticated User
     */
    User login(String email, String password);

    /**
     * Finds a user by ID.
     *
     * @param id user ID
     * @return Optional containing User if found
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by email.
     *
     * @param email user email address
     * @return Optional containing User if found
     */
    Optional<User> findByEmail(String email);
}
