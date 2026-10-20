package com.hirehub.service;

import com.hirehub.dao.JdbcResumeDao;
import com.hirehub.dao.ResumeDao;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Resume;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link ResumeService} with file validation, magic-byte checking,
 * secure UUID file naming, and path traversal prevention.
 */
public class ResumeServiceImpl implements ResumeService {

    public static final String UPLOAD_DIR_PROP = "hirehub.upload.dir";
    private final ResumeDao resumeDao;
    private final File baseStorageDirectory;

    public ResumeServiceImpl() {
        this(new JdbcResumeDao(), null);
    }

    public ResumeServiceImpl(ResumeDao resumeDao) {
        this(resumeDao, null);
    }

    public ResumeServiceImpl(ResumeDao resumeDao, File storageDirectory) {
        this.resumeDao = resumeDao;
        this.baseStorageDirectory = storageDirectory;
    }

    @Override
    public Resume uploadResume(Long seekerId, String originalFileName, InputStream inputStream, long fileSize) {
        if (seekerId == null) {
            throw new ValidationException("Candidate ID is required for resume upload.");
        }
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ValidationException("Please select a file to upload.");
        }
        if (fileSize <= 0) {
            throw new ValidationException("Uploaded file is empty.");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds the 5 MB maximum limit.");
        }

        String simpleName = Paths.get(originalFileName).getFileName().toString().trim();
        if (!simpleName.toLowerCase().endsWith(".pdf")) {
            throw new ValidationException("Only PDF resumes are supported (.pdf).");
        }

        // Validate PDF magic bytes (%PDF-)
        byte[] header = new byte[5];
        InputStream fullStream;
        try {
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            int read = bis.read(header);
            if (read < 5 || header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46 || header[4] != 0x2D) {
                throw new ValidationException("File content is not a valid PDF document.");
            }
            // Reconstruct full stream so header bytes are not lost when saving
            fullStream = new SequenceInputStream(new ByteArrayInputStream(header, 0, read), bis);
        } catch (IOException e) {
            throw new ValidationException("Failed to inspect uploaded file: " + e.getMessage());
        }

        File storageDir = resolveStorageDirectory();
        String safeServerName = UUID.randomUUID().toString() + ".pdf";
        File destination = new File(storageDir, safeServerName);

        try {
            // Guard against directory traversal
            String canonicalDest = destination.getCanonicalPath();
            String canonicalBase = storageDir.getCanonicalPath();
            if (!canonicalDest.startsWith(canonicalBase)) {
                throw new SecurityException("Illegal file path outside storage directory.");
            }

            try (OutputStream out = new FileOutputStream(destination)) {
                fullStream.transferTo(out);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write resume to disk: " + e.getMessage(), e);
        }

        // Clean client filename for display
        String sanitizedClientName = simpleName.replaceAll("[^a-zA-Z0-9._ -]", "_");

        Optional<Resume> existingOpt = resumeDao.findLatestBySeekerId(seekerId);
        if (existingOpt.isPresent()) {
            Resume existing = existingOpt.get();
            // Delete old physical file if it exists
            if (existing.getFilePath() != null) {
                File oldFile = new File(existing.getFilePath());
                if (oldFile.exists() && !oldFile.equals(destination)) {
                    oldFile.delete();
                }
            }
            existing.setFileName(sanitizedClientName);
            existing.setFilePath(destination.getAbsolutePath());
            resumeDao.update(existing);
            return existing;
        } else {
            Resume newResume = new Resume(seekerId, sanitizedClientName, destination.getAbsolutePath());
            return resumeDao.save(newResume);
        }
    }

    @Override
    public Optional<Resume> getCandidateResume(Long seekerId) {
        if (seekerId == null) {
            return Optional.empty();
        }
        return resumeDao.findLatestBySeekerId(seekerId);
    }

    @Override
    public File getResumeFile(Resume resume) {
        if (resume == null || resume.getFilePath() == null) {
            throw new ValidationException("Resume record not found.");
        }
        File file = new File(resume.getFilePath());
        if (!file.exists() || !file.isFile()) {
            throw new ValidationException("The resume file could not be found on the server.");
        }
        return file;
    }

    @Override
    public Optional<Resume> getResumeById(Long resumeId) {
        if (resumeId == null) {
            return Optional.empty();
        }
        return resumeDao.findById(resumeId);
    }

    private File resolveStorageDirectory() {
        if (baseStorageDirectory != null) {
            if (!baseStorageDirectory.exists()) {
                baseStorageDirectory.mkdirs();
            }
            return baseStorageDirectory;
        }

        String configured = System.getProperty(UPLOAD_DIR_PROP);
        File dir;
        if (configured != null && !configured.isBlank()) {
            dir = new File(configured.trim());
        } else {
            String userHome = System.getProperty("user.home", ".");
            dir = new File(userHome, "hirehub_uploads" + File.separator + "resumes");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
