package com.hirehub.dao;

import com.hirehub.exception.DatabaseException;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;
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
 * JDBC implementation of {@link JobDao} using PreparedStatement and connection management.
 */
public class JdbcJobDao implements JobDao {

    private static final String SELECT_BASE =
            "SELECT j.id, j.employer_id, j.title, j.description, j.skills, j.location, j.salary, j.status, j.created_at, "
                    + "ep.company_name, u.name AS employer_name "
                    + "FROM jobs j "
                    + "LEFT JOIN employer_profiles ep ON j.employer_id = ep.user_id "
                    + "LEFT JOIN users u ON j.employer_id = u.id ";

    private final Supplier<Connection> connectionSupplier;

    public JdbcJobDao() {
        this(DBConnection::getConnection);
    }

    public JdbcJobDao(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Optional<Job> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE j.id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToJob(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query job by id: " + id, e);
        }
    }

    @Override
    public List<Job> findAll() {
        String sql = SELECT_BASE + "ORDER BY j.created_at DESC, j.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Job> jobs = new ArrayList<>();
            while (rs.next()) {
                jobs.add(mapRowToJob(rs));
            }
            return jobs;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all jobs", e);
        }
    }

    @Override
    public List<Job> findByStatus(JobStatus status) {
        if (status == null) {
            return findAll();
        }
        String sql = SELECT_BASE + "WHERE j.status = ? ORDER BY j.created_at DESC, j.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<Job> jobs = new ArrayList<>();
                while (rs.next()) {
                    jobs.add(mapRowToJob(rs));
                }
                return jobs;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query jobs by status: " + status, e);
        }
    }

    @Override
    public List<Job> searchJobs(String keyword, String location, JobStatus status) {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND j.status = ? ");
            params.add(status.name());
        }

        if (location != null && !location.isBlank()) {
            sql.append("AND LOWER(j.location) LIKE ? ");
            params.add("%" + location.trim().toLowerCase() + "%");
        }

        if (keyword != null && !keyword.isBlank()) {
            String kwParam = "%" + keyword.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(j.title) LIKE ? OR LOWER(j.description) LIKE ? OR LOWER(j.skills) LIKE ? OR LOWER(ep.company_name) LIKE ?) ");
            params.add(kwParam);
            params.add(kwParam);
            params.add(kwParam);
            params.add(kwParam);
        }

        sql.append("ORDER BY j.created_at DESC, j.id DESC");

        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Job> jobs = new ArrayList<>();
                while (rs.next()) {
                    jobs.add(mapRowToJob(rs));
                }
                return jobs;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search jobs", e);
        }
    }

    @Override
    public List<Job> findByEmployerId(Long employerId) {
        if (employerId == null) {
            return List.of();
        }
        String sql = SELECT_BASE + "WHERE j.employer_id = ? ORDER BY j.created_at DESC, j.id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Job> jobs = new ArrayList<>();
                while (rs.next()) {
                    jobs.add(mapRowToJob(rs));
                }
                return jobs;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query jobs for employer: " + employerId, e);
        }
    }

    @Override
    public List<String> findDistinctLocations() {
        String sql = "SELECT DISTINCT location FROM jobs WHERE location IS NOT NULL AND TRIM(location) <> '' ORDER BY location ASC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> locations = new ArrayList<>();
            while (rs.next()) {
                String loc = rs.getString("location");
                if (loc != null && !loc.isBlank()) {
                    locations.add(loc.trim());
                }
            }
            return locations;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve distinct job locations", e);
        }
    }

    @Override
    public Optional<Job> findByIdAndEmployerId(Long id, Long employerId) {
        if (id == null || employerId == null) {
            return Optional.empty();
        }
        String sql = SELECT_BASE + "WHERE j.id = ? AND j.employer_id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToJob(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find job " + id + " for employer " + employerId, e);
        }
    }

    @Override
    public int countByEmployerId(Long employerId) {
        if (employerId == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM jobs WHERE employer_id = ?";
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
            throw new DatabaseException("Failed to count jobs for employer: " + employerId, e);
        }
    }

    @Override
    public int countActiveByEmployerId(Long employerId) {
        if (employerId == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM jobs WHERE employer_id = ? AND status = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employerId);
            ps.setString(2, JobStatus.APPROVED.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count active jobs for employer: " + employerId, e);
        }
    }

    @Override
    public Job save(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null.");
        }
        String sql = "INSERT INTO jobs (employer_id, title, description, skills, location, salary, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, job.getEmployerId());
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getDescription());
            ps.setString(4, job.getSkills());
            ps.setString(5, job.getLocation());
            ps.setString(6, job.getSalary());
            ps.setString(7, job.getStatus() != null ? job.getStatus().name() : JobStatus.PENDING.name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    job.setId(keys.getLong(1));
                }
            }
            return job;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save job: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Job job) {
        if (job == null || job.getId() == null) {
            throw new IllegalArgumentException("Job and ID cannot be null for update.");
        }
        String sql = "UPDATE jobs SET title = ?, description = ?, skills = ?, location = ?, salary = ?, status = ? WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getSkills());
            ps.setString(4, job.getLocation());
            ps.setString(5, job.getSalary());
            ps.setString(6, job.getStatus() != null ? job.getStatus().name() : JobStatus.PENDING.name());
            ps.setLong(7, job.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update job: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        String sql = "DELETE FROM jobs WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete job with id: " + id, e);
        }
    }

    private Job mapRowToJob(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getLong("id"));
        job.setEmployerId(rs.getLong("employer_id"));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setSkills(rs.getString("skills"));
        job.setLocation(rs.getString("location"));
        job.setSalary(rs.getString("salary"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                job.setStatus(JobStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                job.setStatus(JobStatus.PENDING);
            }
        }

        job.setCreatedAt(rs.getTimestamp("created_at"));

        String company = rs.getString("company_name");
        if (company == null || company.isBlank()) {
            company = rs.getString("employer_name");
        }
        job.setCompanyName(company != null && !company.isBlank() ? company : "HireHub Partner");

        return job;
    }
}
