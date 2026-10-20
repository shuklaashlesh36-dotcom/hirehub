
package com.hirehub.dao;

import com.hirehub.exception.DatabaseException;
import com.hirehub.model.Admin;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * JDBC implementation of UserDao using PreparedStatement
 * and transaction management.
 */
public class JdbcUserDao implements UserDao {

    private static final String SELECT_BASE =
            "SELECT u.id, u.name, u.email, u.password, u.role, u.profile_photo, "
                    + "ep.company_name, ep.company_description, "
                    + "ep.phone AS employer_phone, ep.website AS employer_website, "
                    + "ep.location AS employer_location, "
                    + "jsp.phone AS seeker_phone, jsp.skills, "
                    + "jsp.education, jsp.headline "
                    + "FROM users u "
                    + "LEFT JOIN employer_profiles ep ON u.id = ep.user_id "
                    + "LEFT JOIN job_seeker_profiles jsp ON u.id = jsp.user_id ";

    private final Supplier<Connection> connectionSupplier;

    public JdbcUserDao() {
        this(DBConnection::getConnection);
    }

    public JdbcUserDao(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = SELECT_BASE + "WHERE u.id = ?";

        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logSqlError("findById", e);
            throw new DatabaseException("Failed to query user by id: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String sql = SELECT_BASE + "WHERE u.email = ?";

        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logSqlError("findByEmail", e);
            throw new DatabaseException("Failed to query user by email", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logSqlError("existsByEmail", e);
            throw new DatabaseException(
                    "Failed to check user existence by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = SELECT_BASE + "ORDER BY u.id ASC";

        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<User> users = new ArrayList<>();

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }

            return users;

        } catch (SQLException e) {
            logSqlError("findAll", e);
            throw new DatabaseException("Failed to list all users", e);
        }
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        Connection conn = connectionSupplier.get();

        try {
            conn.setAutoCommit(false);

            String insertUserSql =
                    "INSERT INTO users (name, email, password, role) "
                            + "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(
                    insertUserSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail().trim().toLowerCase());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getRole().name());

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getLong(1));
                    } else {
                        throw new DatabaseException(
                                "Failed to retrieve generated user ID.");
                    }
                }
            }

            if (user instanceof Employer employer) {
                String sql =
                        "INSERT INTO employer_profiles "
                                + "(user_id, company_name, company_description, "
                                + "phone, website, location) "
                                + "VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, user.getId());
                    ps.setString(2, employer.getCompanyName());
                    ps.setString(3, employer.getCompanyDescription());
                    ps.setString(4, employer.getPhone());
                    ps.setString(5, employer.getWebsite());
                    ps.setString(6, employer.getLocation());
                    ps.executeUpdate();
                }

            } else if (user instanceof JobSeeker seeker) {
                String sql =
                        "INSERT INTO job_seeker_profiles "
                                + "(user_id, phone, skills, education, headline) "
                                + "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, user.getId());
                    ps.setString(2, seeker.getPhone());
                    ps.setString(3, seeker.getSkills());
                    ps.setString(4, seeker.getEducation());
                    ps.setString(5, seeker.getHeadline());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return user;

        } catch (SQLException e) {
            logSqlError("save", e);

            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }

            throw new DatabaseException("Failed to save user", e);

        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logSqlError("save-cleanup", e);
            }
        }
    }

    @Override
    public void update(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "User and ID cannot be null for update.");
        }

        Connection conn = connectionSupplier.get();

        try {
            conn.setAutoCommit(false);

            String updateUserSql =
                    "UPDATE users SET name = ?, email = ?, password = ?, "
                            + "role = ?, profile_photo = ? WHERE id = ?";

            try (PreparedStatement ps = conn.prepareStatement(updateUserSql)) {
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail().trim().toLowerCase());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getRole().name());
                ps.setString(5, user.getProfilePhoto());
                ps.setLong(6, user.getId());
                ps.executeUpdate();
            }

            if (user instanceof Employer employer) {
                String updateSql =
                        "UPDATE employer_profiles SET company_name = ?, "
                                + "company_description = ?, phone = ?, "
                                + "website = ?, location = ? WHERE user_id = ?";

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, employer.getCompanyName());
                    ps.setString(2, employer.getCompanyDescription());
                    ps.setString(3, employer.getPhone());
                    ps.setString(4, employer.getWebsite());
                    ps.setString(5, employer.getLocation());
                    ps.setLong(6, user.getId());

                    if (ps.executeUpdate() == 0) {
                        String insertSql =
                                "INSERT INTO employer_profiles "
                                        + "(user_id, company_name, "
                                        + "company_description, phone, website, location) "
                                        + "VALUES (?, ?, ?, ?, ?, ?)";

                        try (PreparedStatement insertPs =
                                     conn.prepareStatement(insertSql)) {
                            insertPs.setLong(1, user.getId());
                            insertPs.setString(2, employer.getCompanyName());
                            insertPs.setString(3, employer.getCompanyDescription());
                            insertPs.setString(4, employer.getPhone());
                            insertPs.setString(5, employer.getWebsite());
                            insertPs.setString(6, employer.getLocation());
                            insertPs.executeUpdate();
                        }
                    }
                }

            } else if (user instanceof JobSeeker seeker) {
                String updateSql =
                        "UPDATE job_seeker_profiles SET phone = ?, skills = ?, "
                                + "education = ?, headline = ? WHERE user_id = ?";

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, seeker.getPhone());
                    ps.setString(2, seeker.getSkills());
                    ps.setString(3, seeker.getEducation());
                    ps.setString(4, seeker.getHeadline());
                    ps.setLong(5, user.getId());

                    if (ps.executeUpdate() == 0) {
                        String insertSql =
                                "INSERT INTO job_seeker_profiles "
                                        + "(user_id, phone, skills, education, headline) "
                                        + "VALUES (?, ?, ?, ?, ?)";

                        try (PreparedStatement insertPs =
                                     conn.prepareStatement(insertSql)) {
                            insertPs.setLong(1, user.getId());
                            insertPs.setString(2, seeker.getPhone());
                            insertPs.setString(3, seeker.getSkills());
                            insertPs.setString(4, seeker.getEducation());
                            insertPs.setString(5, seeker.getHeadline());
                            insertPs.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();

        } catch (SQLException e) {
            logSqlError("update", e);

            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }

            throw new DatabaseException("Failed to update user", e);

        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logSqlError("update-cleanup", e);
            }
        }
    }

    @Override
    public void updateProfilePhoto(Long userId, String profilePhoto) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        String sql = "UPDATE users SET profile_photo = ? WHERE id = ?";

        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, profilePhoto);
            ps.setLong(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            logSqlError("updateProfilePhoto", e);
            throw new DatabaseException(
                    "Failed to update profile photo for user id: " + userId, e);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logSqlError("delete", e);
            throw new DatabaseException(
                    "Failed to delete user with id: " + id, e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String roleStr = rs.getString("role");
        String profilePhoto = rs.getString("profile_photo");

        Role role = Role.valueOf(roleStr);

        User user = switch (role) {
            case ADMIN -> new Admin(id, name, email, password);

            case EMPLOYER -> {
                Employer emp = new Employer(
                        id, name, email, password,
                        rs.getString("company_name"),
                        rs.getString("company_description"));

                emp.setPhone(rs.getString("employer_phone"));
                emp.setWebsite(rs.getString("employer_website"));
                emp.setLocation(rs.getString("employer_location"));

                yield emp;
            }

            case JOB_SEEKER -> {
                JobSeeker seeker = new JobSeeker(
                        id, name, email, password,
                        rs.getString("seeker_phone"),
                        rs.getString("skills"),
                        rs.getString("education"));

                seeker.setHeadline(rs.getString("headline"));
                yield seeker;
            }
        };

        user.setProfilePhoto(profilePhoto);
        return user;
    }

    /**
     * Log SQL exception details without printing passwords or email addresses.
     */
    private static void logSqlError(String operation, SQLException e) {
        System.err.println(
                "HireHub DAO SQL error [" + operation + "]: "
                        + e.getClass().getSimpleName()
                        + ", SQLState=" + e.getSQLState()
                        + ", errorCode=" + e.getErrorCode()
                        + ", message=" + e.getMessage());

        e.printStackTrace(System.err);
    }
}
