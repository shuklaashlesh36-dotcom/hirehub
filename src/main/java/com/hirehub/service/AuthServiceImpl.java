package com.hirehub.service;

import com.hirehub.dao.JdbcUserDao;
import com.hirehub.dao.UserDao;
import com.hirehub.exception.AuthenticationException;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.util.InputValidator;
import com.hirehub.util.PasswordUtil;

import java.util.Optional;

/**
 * Implementation of {@link AuthService} enforcing registration rules and secure authentication.
 */
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;

    public AuthServiceImpl() {
        this(new JdbcUserDao());
    }

    public AuthServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User register(User user) {
        if (user == null) {
            throw new ValidationException("User information must not be empty.");
        }

        // Rule 5: Allow public registration only for EMPLOYER and JOB_SEEKER. Never allow ADMIN registration.
        if (user.getRole() == null || user.getRole() == Role.ADMIN) {
            throw new ValidationException("Public registration as administrator is strictly prohibited.");
        }
        if (user.getRole() != Role.EMPLOYER && user.getRole() != Role.JOB_SEEKER) {
            throw new ValidationException("Invalid registration role: " + user.getRole());
        }

        String validName = InputValidator.validateName(user.getName());
        String validEmail = InputValidator.validateEmail(user.getEmail());
        String rawPassword = InputValidator.validatePassword(user.getPassword());

        user.setName(validName);
        user.setEmail(validEmail);

        if (user instanceof Employer employer) {
            String company = InputValidator.requireText("Company Name", employer.getCompanyName());
            employer.setCompanyName(company);
        } else if (user instanceof JobSeeker seeker) {
            if (seeker.getPhone() != null && !seeker.getPhone().isBlank()) {
                seeker.setPhone(seeker.getPhone().trim());
            }
        }

        if (userDao.existsByEmail(validEmail)) {
            throw new ValidationException("An account with email '" + validEmail + "' already exists.");
        }

        // Hash password before saving
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        user.setPassword(hashedPassword);

        return userDao.save(user);
    }

    @Override
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Email and password are required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userDao.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // Rule 11 & Security: Transparently rehash legacy passwords (like initial admin seeds)
        if (PasswordUtil.needsRehash(user.getPassword())) {
            user.setPassword(PasswordUtil.hashPassword(password));
            userDao.update(user);
        }

        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userDao.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userDao.findByEmail(email.trim().toLowerCase());
    }
}
