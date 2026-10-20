package com.hirehub.service;

import com.hirehub.dao.ApplicationDao;
import com.hirehub.dao.JdbcApplicationDao;
import com.hirehub.dao.JdbcUserDao;
import com.hirehub.dao.UserDao;
import com.hirehub.exception.UserNotFoundException;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Application;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.util.InputValidator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link ProfileService} providing profile updates, image magic-byte
 * validation, secure UUID avatar storage, and role-based access checks.
 */
public class ProfileServiceImpl implements ProfileService {

    public static final String AVATAR_DIR_PROP = "hirehub.avatar.dir";

    private final UserDao userDao;
    private final ApplicationDao applicationDao;
    private final File baseStorageDirectory;

    public ProfileServiceImpl() {
        this(new JdbcUserDao(), new JdbcApplicationDao(), null);
    }

    public ProfileServiceImpl(UserDao userDao, ApplicationDao applicationDao) {
        this(userDao, applicationDao, null);
    }

    public ProfileServiceImpl(UserDao userDao, ApplicationDao applicationDao, File storageDirectory) {
        this.userDao = userDao;
        this.applicationDao = applicationDao;
        this.baseStorageDirectory = storageDirectory;
    }

    @Override
    public Optional<User> getUserProfile(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userDao.findById(userId);
    }

    @Override
    public User updateCandidateProfile(Long userId, String name, String phone, String education,
                                       String skills, String headline) {
        if (userId == null) {
            throw new ValidationException("User ID is required.");
        }
        User existing = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!(existing instanceof JobSeeker seeker)) {
            throw new ValidationException("User is not a candidate profile.");
        }

        String validName = InputValidator.validateName(name);
        seeker.setName(validName);

        if (phone != null && !phone.isBlank()) {
            String trimmedPhone = phone.trim();
            if (trimmedPhone.length() > 20) {
                throw new ValidationException("Phone number must not exceed 20 characters.");
            }
            seeker.setPhone(trimmedPhone);
        } else {
            seeker.setPhone(null);
        }

        if (education != null && !education.isBlank()) {
            String trimmedEdu = education.trim();
            if (trimmedEdu.length() > 100) {
                throw new ValidationException("Education must not exceed 100 characters.");
            }
            seeker.setEducation(trimmedEdu);
        } else {
            seeker.setEducation(null);
        }

        if (skills != null && !skills.isBlank()) {
            String trimmedSkills = skills.trim();
            if (trimmedSkills.length() > 255) {
                throw new ValidationException("Skills must not exceed 255 characters.");
            }
            seeker.setSkills(trimmedSkills);
        } else {
            seeker.setSkills(null);
        }

        if (headline != null && !headline.isBlank()) {
            String trimmedHeadline = headline.trim();
            if (trimmedHeadline.length() > 200) {
                throw new ValidationException("Headline / bio must not exceed 200 characters.");
            }
            seeker.setHeadline(trimmedHeadline);
        } else {
            seeker.setHeadline(null);
        }

        userDao.update(seeker);
        return seeker;
    }

    @Override
    public User updateEmployerProfile(Long userId, String name, String companyName, String companyDescription,
                                      String phone, String website, String location) {
        if (userId == null) {
            throw new ValidationException("User ID is required.");
        }
        User existing = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!(existing instanceof Employer employer)) {
            throw new ValidationException("User is not an employer profile.");
        }

        String validName = InputValidator.validateName(name);
        employer.setName(validName);

        String validCompany = InputValidator.requireText("Company Name", companyName);
        if (validCompany.length() > 100) {
            throw new ValidationException("Company Name must not exceed 100 characters.");
        }
        employer.setCompanyName(validCompany);

        if (companyDescription != null && !companyDescription.isBlank()) {
            String desc = companyDescription.trim();
            if (desc.length() > 1000) {
                throw new ValidationException("Company description must not exceed 1000 characters.");
            }
            employer.setCompanyDescription(desc);
        } else {
            employer.setCompanyDescription(null);
        }

        if (phone != null && !phone.isBlank()) {
            String trimmedPhone = phone.trim();
            if (trimmedPhone.length() > 20) {
                throw new ValidationException("Contact phone must not exceed 20 characters.");
            }
            employer.setPhone(trimmedPhone);
        } else {
            employer.setPhone(null);
        }

        if (website != null && !website.isBlank()) {
            String trimmedWeb = website.trim();
            if (trimmedWeb.length() > 150) {
                throw new ValidationException("Website must not exceed 150 characters.");
            }
            employer.setWebsite(trimmedWeb);
        } else {
            employer.setWebsite(null);
        }

        if (location != null && !location.isBlank()) {
            String trimmedLoc = location.trim();
            if (trimmedLoc.length() > 100) {
                throw new ValidationException("Location must not exceed 100 characters.");
            }
            employer.setLocation(trimmedLoc);
        } else {
            employer.setLocation(null);
        }

        userDao.update(employer);
        return employer;
    }

    @Override
    public String uploadProfilePhoto(Long userId, String originalFilename, String contentType,
                                     InputStream inputStream, long fileSize) {
        if (userId == null) {
            throw new ValidationException("User ID is required for photo upload.");
        }
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ValidationException("Please select an image file to upload.");
        }
        if (fileSize <= 0) {
            throw new ValidationException("Uploaded file is empty.");
        }
        if (fileSize > MAX_AVATAR_SIZE) {
            throw new ValidationException("Image file size exceeds the 2 MB maximum limit.");
        }

        String simpleName = Paths.get(originalFilename).getFileName().toString().trim().toLowerCase();
        String extension;
        if (simpleName.endsWith(".jpg") || simpleName.endsWith(".jpeg")) {
            extension = ".jpg";
        } else if (simpleName.endsWith(".png")) {
            extension = ".png";
        } else if (simpleName.endsWith(".webp")) {
            extension = ".webp";
        } else {
            throw new ValidationException("Only JPEG, PNG, or WebP images are supported (.jpg, .jpeg, .png, .webp).");
        }

        // Validate image magic bytes
        byte[] header = new byte[16];
        InputStream fullStream;
        try {
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            int read = bis.read(header);
            if (read < 12) {
                throw new ValidationException("Image file is too small or corrupted.");
            }

            boolean isJpeg = (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF);
            boolean isPng = (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47);
            boolean isWebp = (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P');

            if (!isJpeg && !isPng && !isWebp) {
                throw new ValidationException("Uploaded file does not match valid JPEG, PNG, or WebP image data.");
            }

            // Cross-check extension with content
            if (isJpeg && !extension.equals(".jpg")) {
                extension = ".jpg";
            } else if (isPng && !extension.equals(".png")) {
                extension = ".png";
            } else if (isWebp && !extension.equals(".webp")) {
                extension = ".webp";
            }

            fullStream = new SequenceInputStream(new ByteArrayInputStream(header, 0, read), bis);
        } catch (IOException e) {
            throw new ValidationException("Failed to inspect uploaded image: " + e.getMessage());
        }

        File storageDir = resolveStorageDirectory();
        String safeServerName = UUID.randomUUID().toString() + extension;
        File destination = new File(storageDir, safeServerName);

        try {
            String canonicalDest = destination.getCanonicalPath();
            String canonicalBase = storageDir.getCanonicalPath();
            if (!canonicalDest.startsWith(canonicalBase)) {
                throw new SecurityException("Illegal file path outside storage directory.");
            }

            try (OutputStream out = new FileOutputStream(destination)) {
                fullStream.transferTo(out);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write image to disk: " + e.getMessage(), e);
        }

        // Delete previous avatar file if exists
        String oldPhoto = user.getProfilePhoto();
        if (oldPhoto != null && !oldPhoto.isBlank()) {
            File oldFile = new File(storageDir, oldPhoto);
            if (oldFile.exists() && !oldFile.equals(destination)) {
                oldFile.delete();
            }
        }

        userDao.updateProfilePhoto(userId, safeServerName);
        user.setProfilePhoto(safeServerName);
        return safeServerName;
    }

    @Override
    public void removeProfilePhoto(Long userId) {
        if (userId == null) {
            throw new ValidationException("User ID is required.");
        }
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        String oldPhoto = user.getProfilePhoto();
        if (oldPhoto != null && !oldPhoto.isBlank()) {
            File storageDir = resolveStorageDirectory();
            File oldFile = new File(storageDir, oldPhoto);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        userDao.updateProfilePhoto(userId, null);
        user.setProfilePhoto(null);
    }

    @Override
    public File getProfilePhotoFile(String photoFileName) {
        if (photoFileName == null || photoFileName.isBlank()) {
            throw new ValidationException("Photo file name is required.");
        }
        if (photoFileName.contains("..") || photoFileName.contains("/") || photoFileName.contains("\\")) {
            throw new SecurityException("Invalid photo filename.");
        }
        File storageDir = resolveStorageDirectory();
        File file = new File(storageDir, photoFileName);
        if (!file.exists() || !file.isFile()) {
            throw new ValidationException("Profile photo file not found on disk.");
        }
        return file;
    }

    @Override
    public boolean canViewProfilePhoto(User requester, Long targetUserId) {
        if (requester == null) {
            return false;
        }
        if (targetUserId == null || targetUserId.equals(requester.getId())) {
            return true;
        }
        if (requester.getRole() == Role.ADMIN) {
            return true;
        }
        if (requester.getRole() == Role.EMPLOYER) {
            // Employer can view photo if target candidate applied to any of employer's jobs
            List<Application> applications = applicationDao.findByEmployerId(requester.getId());
            for (Application app : applications) {
                if (app.getSeekerId() != null && app.getSeekerId().equals(targetUserId)) {
                    return true;
                }
            }
            return false;
        }
        if (requester.getRole() == Role.JOB_SEEKER) {
            // Candidate can view company avatar of any employer
            Optional<User> targetOpt = userDao.findById(targetUserId);
            return targetOpt.isPresent() && targetOpt.get().getRole() == Role.EMPLOYER;
        }
        return false;
    }

    private File resolveStorageDirectory() {
        if (baseStorageDirectory != null) {
            if (!baseStorageDirectory.exists()) {
                baseStorageDirectory.mkdirs();
            }
            return baseStorageDirectory;
        }

        String configured = System.getProperty(AVATAR_DIR_PROP);
        File dir;
        if (configured != null && !configured.isBlank()) {
            dir = new File(configured.trim());
        } else {
            String userHome = System.getProperty("user.home", ".");
            dir = new File(userHome, "hirehub_uploads" + File.separator + "avatars");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
