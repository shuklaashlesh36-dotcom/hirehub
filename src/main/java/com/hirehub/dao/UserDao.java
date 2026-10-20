package com.hirehub.dao;

import com.hirehub.model.User;

import java.util.Optional;

/**
 * Persistence contract for user accounts and associated role-specific profiles.
 */
public interface UserDao extends GenericDao<User, Long> {

    /**
     * Finds a user and their role-specific profile by email address.
     *
     * @param email normalized user email
     * @return Optional containing the User if found, or empty Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email address.
     *
     * @param email normalized user email
     * @return true if an account exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Updates only the profile photo filename for a user.
     *
     * @param userId user identifier
     * @param profilePhoto safe filename or null to clear
     */
    void updateProfilePhoto(Long userId, String profilePhoto);
}
