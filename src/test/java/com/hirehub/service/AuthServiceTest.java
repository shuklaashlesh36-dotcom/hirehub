package com.hirehub.service;

import com.hirehub.dao.UserDao;
import com.hirehub.exception.AuthenticationException;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Admin;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private InMemoryUserDao inMemoryUserDao;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        inMemoryUserDao = new InMemoryUserDao();
        authService = new AuthServiceImpl(inMemoryUserDao);
    }

    @Test
    void register_withValidEmployer_succeedsAndHashesPassword() {
        Employer employer = new Employer("Acme Recruiter", "recruiter@acme.com", "Password@123",
                "Acme Corp", "Tech Solutions");

        User registered = authService.register(employer);

        assertNotNull(registered.getId());
        assertEquals("recruiter@acme.com", registered.getEmail());
        assertNotNull(registered.getPassword());
        assertTrue(registered.getPassword().contains(":"), "Password must be hashed with PBKDF2 salt.");
        assertTrue(PasswordUtil.verifyPassword("Password@123", registered.getPassword()));
    }

    @Test
    void register_withValidJobSeeker_succeeds() {
        JobSeeker seeker = new JobSeeker("John Doe", "john@example.com", "SecretPass123",
                "9876543210", "Java, SQL", "B.Tech");

        User registered = authService.register(seeker);

        assertNotNull(registered.getId());
        assertEquals(Role.JOB_SEEKER, registered.getRole());
        assertTrue(PasswordUtil.verifyPassword("SecretPass123", registered.getPassword()));
    }

    @Test
    void register_attemptAsAdmin_strictlyProhibited() {
        Admin admin = new Admin("Rogue Admin", "admin@evil.com", "MasterKey123");

        ValidationException ex = assertThrows(ValidationException.class, () -> authService.register(admin));
        assertTrue(ex.getMessage().contains("administrator is strictly prohibited"));
    }

    @Test
    void register_duplicateEmail_throwsValidationException() {
        Employer employer1 = new Employer("First", "duplicate@test.com", "Password@123", "Co1", "Desc");
        authService.register(employer1);

        JobSeeker employer2 = new JobSeeker("Second", "duplicate@test.com", "Password@456", "123", "Java", "Degree");
        ValidationException ex = assertThrows(ValidationException.class, () -> authService.register(employer2));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void register_shortPassword_throwsValidationException() {
        JobSeeker seeker = new JobSeeker("Shorty", "short@test.com", "pass", "123", "Java", "Degree");
        assertThrows(ValidationException.class, () -> authService.register(seeker));
    }

    @Test
    void login_withCorrectCredentials_succeeds() {
        Employer employer = new Employer("Alice", "alice@corp.com", "ValidPass123", "Corp", "IT");
        authService.register(employer);

        User loggedIn = authService.login("alice@corp.com", "ValidPass123");
        assertNotNull(loggedIn);
        assertEquals("alice@corp.com", loggedIn.getEmail());
    }

    @Test
    void login_withWrongPassword_throwsAuthenticationException() {
        Employer employer = new Employer("Alice", "alice@corp.com", "ValidPass123", "Corp", "IT");
        authService.register(employer);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login("alice@corp.com", "WrongPassword!"));
        assertEquals("Invalid email or password.", ex.getMessage());
    }

    @Test
    void login_withUnknownEmail_throwsAuthenticationException() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login("nonexistent@corp.com", "SomePass123"));
        assertEquals("Invalid email or password.", ex.getMessage());
    }

    @Test
    void login_withLegacyPlaintextAdmin_authenticatesAndRehashesPassword() {
        // Initial setup seed with legacy plaintext
        Admin admin = new Admin(100L, "Super Admin", "admin@hirehub.com", "ChangeMe@123");
        inMemoryUserDao.saveDirectly(admin);

        // First login: authenticates with legacy plain text and automatically updates password in DAO
        User loggedIn = authService.login("admin@hirehub.com", "ChangeMe@123");
        assertNotNull(loggedIn);

        // Check that the password in storage is now a PBKDF2 hash
        User updatedInDb = inMemoryUserDao.findById(100L).orElseThrow();
        assertTrue(updatedInDb.getPassword().contains(":"), "Password should have been re-hashed with PBKDF2.");
        assertFalse(PasswordUtil.needsRehash(updatedInDb.getPassword()));
        assertTrue(PasswordUtil.verifyPassword("ChangeMe@123", updatedInDb.getPassword()));
    }

    /**
     * In-memory test double for {@link UserDao} to enable hermetic unit tests.
     */
    private static class InMemoryUserDao implements UserDao {
        private final Map<Long, User> byId = new ConcurrentHashMap<>();
        private final Map<String, User> byEmail = new ConcurrentHashMap<>();
        private final AtomicLong idSequence = new AtomicLong(1);

        void saveDirectly(User user) {
            byId.put(user.getId(), user);
            byEmail.put(user.getEmail().toLowerCase(), user);
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email.toLowerCase()));
        }

        @Override
        public boolean existsByEmail(String email) {
            return byEmail.containsKey(email.toLowerCase());
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public User save(User user) {
            long id = idSequence.getAndIncrement();
            user.setId(id);
            byId.put(id, user);
            byEmail.put(user.getEmail().toLowerCase(), user);
            return user;
        }

        @Override
        public void update(User user) {
            byId.put(user.getId(), user);
            byEmail.put(user.getEmail().toLowerCase(), user);
        }

        @Override
        public void updateProfilePhoto(Long userId, String profilePhoto) {
            User user = byId.get(userId);
            if (user != null) {
                user.setProfilePhoto(profilePhoto);
            }
        }

        @Override
        public void delete(Long id) {
            User removed = byId.remove(id);
            if (removed != null) {
                byEmail.remove(removed.getEmail().toLowerCase());
            }
        }
    }
}
