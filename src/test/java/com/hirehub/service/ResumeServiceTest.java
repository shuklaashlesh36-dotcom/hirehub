package com.hirehub.service;

import com.hirehub.dao.ResumeDao;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Resume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeServiceTest {

    @TempDir
    File tempDir;

    private InMemoryResumeDao resumeDao;
    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        resumeDao = new InMemoryResumeDao();
        resumeService = new ResumeServiceImpl(resumeDao, tempDir);
    }

    @Test
    void uploadResume_validPdf_storesFileAndSavesEntity() {
        byte[] pdfContent = "%PDF-1.4 sample content".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(pdfContent);

        Resume saved = resumeService.uploadResume(10L, "john_resume.pdf", in, pdfContent.length);

        assertNotNull(saved);
        assertEquals(10L, saved.getSeekerId());
        assertEquals("john_resume.pdf", saved.getFileName());
        assertNotNull(saved.getFilePath());

        File storedFile = resumeService.getResumeFile(saved);
        assertTrue(storedFile.exists());
        assertTrue(storedFile.length() > 0);
    }

    @Test
    void uploadResume_rejectsNonPdfExtension() {
        byte[] content = "%PDF-1.4 content".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                resumeService.uploadResume(10L, "document.docx", in, content.length)
        );
        assertTrue(ex.getMessage().contains("Only PDF resumes are supported"));
    }

    @Test
    void uploadResume_rejectsInvalidMagicBytes() {
        byte[] fakePdf = "NOT_A_REAL_PDF_HEADER".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(fakePdf);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                resumeService.uploadResume(10L, "fake.pdf", in, fakePdf.length)
        );
        assertTrue(ex.getMessage().contains("not a valid PDF document"));
    }

    @Test
    void uploadResume_rejectsEmptyFile() {
        InputStream in = new ByteArrayInputStream(new byte[0]);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                resumeService.uploadResume(10L, "empty.pdf", in, 0)
        );
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    void uploadResume_rejectsOversizedFile() {
        InputStream in = new ByteArrayInputStream("%PDF-1.4".getBytes(StandardCharsets.UTF_8));
        long oversized = (6 * 1024 * 1024); // 6 MB

        ValidationException ex = assertThrows(ValidationException.class, () ->
                resumeService.uploadResume(10L, "large.pdf", in, oversized)
        );
        assertTrue(ex.getMessage().contains("5 MB maximum limit"));
    }

    @Test
    void uploadResume_replacesExistingResumeRecord() {
        byte[] pdf1 = "%PDF-1.4 First".getBytes(StandardCharsets.UTF_8);
        Resume first = resumeService.uploadResume(20L, "v1.pdf", new ByteArrayInputStream(pdf1), pdf1.length);
        assertEquals("v1.pdf", first.getFileName());

        byte[] pdf2 = "%PDF-1.4 Second".getBytes(StandardCharsets.UTF_8);
        Resume second = resumeService.uploadResume(20L, "v2.pdf", new ByteArrayInputStream(pdf2), pdf2.length);
        assertEquals("v2.pdf", second.getFileName());

        Optional<Resume> latest = resumeService.getCandidateResume(20L);
        assertTrue(latest.isPresent());
        assertEquals("v2.pdf", latest.get().getFileName());
    }

    private static class InMemoryResumeDao implements ResumeDao {
        private final List<Resume> resumes = new ArrayList<>();
        private long idGen = 1;

        @Override
        public Optional<Resume> findLatestBySeekerId(Long seekerId) {
            return resumes.stream()
                    .filter(r -> r.getSeekerId().equals(seekerId))
                    .reduce((first, second) -> second);
        }

        @Override
        public Optional<Resume> findById(Long id) {
            return resumes.stream().filter(r -> r.getId().equals(id)).findFirst();
        }

        @Override
        public List<Resume> findAll() {
            return new ArrayList<>(resumes);
        }

        @Override
        public Resume save(Resume entity) {
            entity.setId(idGen++);
            resumes.add(entity);
            return entity;
        }

        @Override
        public void update(Resume entity) {
            findById(entity.getId()).ifPresent(existing -> {
                int idx = resumes.indexOf(existing);
                resumes.set(idx, entity);
            });
        }

        @Override
        public void delete(Long id) {
            resumes.removeIf(r -> r.getId().equals(id));
        }
    }
}
