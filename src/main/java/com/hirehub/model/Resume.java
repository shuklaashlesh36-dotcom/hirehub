package com.hirehub.model;

public class Resume {

    private Long id;
    private Long seekerId;
    private String fileName;
    private String filePath;

    public Resume() {
    }

    public Resume(Long seekerId, String fileName, String filePath) {
        this(null, seekerId, fileName, filePath);
    }

    public Resume(Long id, Long seekerId, String fileName, String filePath) {
        this.id = id;
        this.seekerId = seekerId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeekerId() {
        return seekerId;
    }

    public void setSeekerId(Long seekerId) {
        this.seekerId = seekerId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private java.sql.Timestamp uploadedAt;

    public Resume(Long id, Long seekerId, String fileName, String filePath, java.sql.Timestamp uploadedAt) {
        this.id = id;
        this.seekerId = seekerId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }

    public java.sql.Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(java.sql.Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return "Resume{id=" + id
                + ", seekerId=" + seekerId
                + ", fileName='" + fileName + '\''
                + ", uploadedAt=" + uploadedAt
                + '}';
    }
}
