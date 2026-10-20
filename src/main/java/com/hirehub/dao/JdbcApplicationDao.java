package com.hirehub.dao;

import com.hirehub.exception.DatabaseException;
import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import com.hirehub.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * JDBC implementation of {@link ApplicationDao} with polymorphic joins for candidate dashboards.
 */
public class JdbcApplicationDao implements ApplicationDao {

    private static final String SELECT_BASE =
            "SELECT a.id, a.job_id, a.seeker_id, a.resume_id, a.status, a.applied_at, "
                    + "j.title AS job_title, j.location AS job_location, "
                    + "ep.company_name, u.name AS employer_name, "
                    + "r.file_name AS resume_file_name, "
                    + "su.name AS seeker_name, su.email AS seeker_email, "
                    + "jsp.phone AS seeker_phone "
                    + "FROM applications a "
                    + "JOIN jobs j ON a.job_id = j.id "
                    + "LEFT JOIN employer_profiles ep ON j.employer_id = ep.user_id "
                    + "LEFT JOIN users u ON j.employer_id = u.id "
                    + "LEFT JOIN resumes r ON a.resume_id = r.id "
                    + "LEFT JOIN users su ON a.seeker_id = su.id "
                    + "LEFT JOIN job_seeker_profiles jsp ON a.seeker_id = jsp.user_id ";

    private final Supplier<Connection> connectionSupplier;

    public JdbcApplicationDao() {
        this(DBConnection::getConnection);
    }

    public JdbcApplicationDao(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Optional<Application> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE a.id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToApplication(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find application by id: " + id, e);
        }
    }

    @Override
    public List<Application> findBySeekerId(Long seekerId) {
        if (seekerId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE a.seeker_id = ? ORDER BY a.applied_at DESC, a.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seekerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Application> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRowToApplication(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find applications for seeker: " + seekerId, e);
        }
    }

    @Override
    public boolean existsByJobIdAndSeekerId(Long jobId, Long seekerId) {
        if (jobId == null || seekerId == null) {
            return false;
        }
        String sql = "SELECT 1 FROM applications WHERE job_id = ? AND seeker_id = ? LIMIT 1";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            ps.setLong(2, seekerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check application existence for job=" + jobId + " seeker=" + seekerId, e);
        }
    }

    @Override
    public List<Application> findByJobId(Long jobId) {
        if (jobId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE a.job_id = ? ORDER BY a.applied_at DESC, a.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Application> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRowToApplication(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find applications for job: " + jobId, e);
        }
    }

    @Override
    public List<Application> findByEmployerId(Long employerId) {
        if (employerId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE j.employer_id = ? ORDER BY a.applied_at DESC, a.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Application> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRowToApplication(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find applications for employer: " + employerId, e);
        }
    }

    @Override
    public List<Application> findByJobIdAndEmployerId(Long jobId, Long employerId) {
        if (jobId == null || employerId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE a.job_id = ? AND j.employer_id = ? ORDER BY a.applied_at DESC, a.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            ps.setLong(2, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Application> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRowToApplication(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find applications for job: " + jobId + " employer: " + employerId, e);
        }
    }

    @Override
    public Optional<Application> findByIdAndEmployerId(Long applicationId, Long employerId) {
        if (applicationId == null || employerId == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE a.id = ? AND j.employer_id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            ps.setLong(2, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToApplication(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find application " + applicationId + " for employer " + employerId, e);
        }
    }

    @Override
    public int countApplicantsByEmployerId(Long employerId) {
        if (employerId == null) {
            return 0;
        }
        String sql = "SELECT COUNT(a.id) FROM applications a JOIN jobs j ON a.job_id = j.id WHERE j.employer_id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count applicants for employer: " + employerId, e);
        }
    }

    @Override
    public List<Application> findRecentByEmployerId(Long employerId, int limit) {
        if (employerId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE j.employer_id = ? ORDER BY a.applied_at DESC, a.id DESC LIMIT ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employerId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<Application> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRowToApplication(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find recent applications for employer: " + employerId, e);
        }
    }

    @Override
    public int countByJobId(Long jobId) {
        if (jobId == null) {
            return 0;
        }
        String sql = "SELECT COUNT(id) FROM applications WHERE job_id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count applications for job: " + jobId, e);
        }
    }

    @Override
    public List<Application> findAll() {
        String sql = SELECT_BASE + "ORDER BY a.applied_at DESC, a.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Application> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRowToApplication(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all applications", e);
        }
    }

    @Override
    public Application save(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("Application cannot be null.");
        }
        String sql = "INSERT INTO applications (job_id, seeker_id, resume_id, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, application.getJobId());
            ps.setLong(2, application.getSeekerId());
            if (application.getResumeId() != null) {
                ps.setLong(3, application.getResumeId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setString(4, application.getStatus() != null ? application.getStatus().name() : ApplicationStatus.APPLIED.name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    application.setId(keys.getLong(1));
                }
            }
            return application;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save application: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Application application) {
        if (application == null || application.getId() == null) {
            throw new IllegalArgumentException("Application and ID cannot be null for update.");
        }
        String sql = "UPDATE applications SET resume_id = ?, status = ? WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (application.getResumeId() != null) {
                ps.setLong(1, application.getResumeId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, application.getStatus() != null ? application.getStatus().name() : ApplicationStatus.APPLIED.name());
            ps.setLong(3, application.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update application: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        String sql = "DELETE FROM applications WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete application with id: " + id, e);
        }
    }

    private Application mapRowToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getLong("id"));
        app.setJobId(rs.getLong("job_id"));
        app.setSeekerId(rs.getLong("seeker_id"));

        long resumeIdVal = rs.getLong("resume_id");
        if (!rs.wasNull()) {
            app.setResumeId(resumeIdVal);
        }

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                app.setStatus(ApplicationStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                app.setStatus(ApplicationStatus.APPLIED);
            }
        }

        app.setAppliedAt(rs.getTimestamp("applied_at"));
        app.setJobTitle(rs.getString("job_title"));
        app.setJobLocation(rs.getString("job_location"));

        String company = rs.getString("company_name");
        if (company == null || company.isBlank()) {
            company = rs.getString("employer_name");
        }
        app.setCompanyName(company != null && !company.isBlank() ? company : "HireHub Partner");

        app.setResumeFileName(rs.getString("resume_file_name"));

        try {
            app.setSeekerName(rs.getString("seeker_name"));
            app.setSeekerEmail(rs.getString("seeker_email"));
            app.setSeekerPhone(rs.getString("seeker_phone"));
        } catch (SQLException ignored) {
            // Seeker fields might be absent in some specialized queries
        }

        return app;
    }
}
