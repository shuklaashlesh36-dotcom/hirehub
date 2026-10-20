package com.hirehub.dao;

import com.hirehub.exception.DatabaseException;
import com.hirehub.model.Resume;
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
 * JDBC implementation of {@link ResumeDao}.
 */
public class JdbcResumeDao implements ResumeDao {

    private final Supplier<Connection> connectionSupplier;

    public JdbcResumeDao() {
        this(DBConnection::getConnection);
    }

    public JdbcResumeDao(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public Optional<Resume> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, seeker_id, file_name, file_path, uploaded_at FROM resumes WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToResume(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find resume by id: " + id, e);
        }
    }

    @Override
    public Optional<Resume> findLatestBySeekerId(Long seekerId) {
        if (seekerId == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, seeker_id, file_name, file_path, uploaded_at FROM resumes WHERE seeker_id = ? ORDER BY uploaded_at DESC, id DESC LIMIT 1";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seekerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToResume(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find latest resume for seeker: " + seekerId, e);
        }
    }

    @Override
    public List<Resume> findAll() {
        String sql = "SELECT id, seeker_id, file_name, file_path, uploaded_at FROM resumes ORDER BY uploaded_at DESC, id DESC";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Resume> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRowToResume(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list resumes", e);
        }
    }

    @Override
    public Resume save(Resume resume) {
        if (resume == null) {
            throw new IllegalArgumentException("Resume cannot be null.");
        }
        String sql = "INSERT INTO resumes (seeker_id, file_name, file_path) VALUES (?, ?, ?)";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, resume.getSeekerId());
            ps.setString(2, resume.getFileName());
            ps.setString(3, resume.getFilePath());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    resume.setId(keys.getLong(1));
                }
            }
            return resume;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save resume: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Resume resume) {
        if (resume == null || resume.getId() == null) {
            throw new IllegalArgumentException("Resume and ID cannot be null for update.");
        }
        String sql = "UPDATE resumes SET file_name = ?, file_path = ? WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resume.getFileName());
            ps.setString(2, resume.getFilePath());
            ps.setLong(3, resume.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update resume: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        String sql = "DELETE FROM resumes WHERE id = ?";
        try (Connection conn = connectionSupplier.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete resume by id: " + id, e);
        }
    }

    private Resume mapRowToResume(ResultSet rs) throws SQLException {
        Resume resume = new Resume();
        resume.setId(rs.getLong("id"));
        resume.setSeekerId(rs.getLong("seeker_id"));
        resume.setFileName(rs.getString("file_name"));
        resume.setFilePath(rs.getString("file_path"));
        resume.setUploadedAt(rs.getTimestamp("uploaded_at"));
        return resume;
    }
}
