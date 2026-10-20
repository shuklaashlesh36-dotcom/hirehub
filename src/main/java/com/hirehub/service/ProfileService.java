package com.hirehub.service;

import com.hirehub.model.User;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

/**
 * Service contract for candidate and employer profile management,
 * including profile photo upload, validation, and retrieval.
 */
public interface ProfileService {

    long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2 MB

    /**
     * Retrieves user profile details by ID.
     *
     * @param userId user ID
     * @return optional containing the user
     */
    Optional<User> getUserProfile(Long userId);

    /**
     * Updates candidate profile information.
     *
     * @param userId    candidate user ID
     * @param name      full name
     * @param phone     contact phone
     * @param education academic background
     * @param skills    technical/soft skills
     * @param headline  professional headline or bio
     * @return updated User
     */
    User updateCandidateProfile(Long userId, String name, String phone, String education, String skills, String headline);

    /**
     * Updates employer profile information.
     *
     * @param userId             employer user ID
     * @param name               contact person's name
     * @param companyName        company name
     * @param companyDescription description of company
     * @param phone              contact phone
     * @param website            company website
     * @param location           headquarters or operating location
     * @return updated User
     */
    User updateEmployerProfile(Long userId, String name, String companyName, String companyDescription,
                               String phone, String website, String location);

    /**
     * Uploads and validates a profile photo (JPEG, PNG, WebP up to 2MB).
     *
     * @param userId           user ID
     * @param originalFilename original name from client
     * @param contentType      MIME content type from header
     * @param inputStream      data stream of the file
     * @param fileSize         size in bytes
     * @return safe server-side file name
     */
    String uploadProfilePhoto(Long userId, String originalFilename, String contentType,
                              InputStream inputStream, long fileSize);

    /**
     * Removes the user's profile photo and resets to default avatar.
     *
     * @param userId user ID
     */
    void removeProfilePhoto(Long userId);

    /**
     * Retrieves the profile photo file from the safe storage directory.
     *
     * @param photoFileName server-side photo filename
     * @return existing File
     */
    File getProfilePhotoFile(String photoFileName);

    /**
     * Validates whether the given user is authorized to view the target user's profile photo.
     *
     * @param requester    currently authenticated user
     * @param targetUserId user whose photo is requested
     * @return true if authorized, false otherwise
     */
    boolean canViewProfilePhoto(User requester, Long targetUserId);
}
