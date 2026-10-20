package com.hirehub.service;

import com.hirehub.model.Resume;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

/**
 * Service for candidate resume upload, validation, secure storage, and retrieval.
 */
public interface ResumeService {

    long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    /**
     * Validates and securely stores a candidate's uploaded PDF resume.
     *
     * @param seekerId         candidate user ID
     * @param originalFileName client-provided filename
     * @param inputStream      file content input stream
     * @param fileSize         file size in bytes
     * @return saved Resume entity with metadata
     */
    Resume uploadResume(Long seekerId, String originalFileName, InputStream inputStream, long fileSize);

    /**
     * Retrieves the latest resume for a candidate.
     *
     * @param seekerId candidate user ID
     * @return Resume if present
     */
    Optional<Resume> getCandidateResume(Long seekerId);

    /**
     * Resolves the secure file on disk for a given resume entity.
     *
     * @param resume Resume entity
     * @return File on disk
     */
    File getResumeFile(Resume resume);

    /**
     * Retrieves a resume by its primary ID.
     *
     * @param resumeId resume ID
     * @return Resume if present
     */
    Optional<Resume> getResumeById(Long resumeId);
}
