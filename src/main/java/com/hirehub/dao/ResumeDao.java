package com.hirehub.dao;

import com.hirehub.model.Resume;

import java.util.Optional;

/**
 * Persistence contract for candidate resumes.
 */
public interface ResumeDao extends GenericDao<Resume, Long> {

    /**
     * Finds the most recently uploaded resume for a given candidate.
     *
     * @param seekerId candidate user ID
     * @return candidate's resume if present
     */
    Optional<Resume> findLatestBySeekerId(Long seekerId);
}
