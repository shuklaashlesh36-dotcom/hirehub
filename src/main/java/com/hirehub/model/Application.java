package com.hirehub.model;

public class Application {

    private Long id;
    private Long jobId;
    private Long seekerId;
    private Long resumeId;
    private ApplicationStatus status;

    public Application() {
        this.status = ApplicationStatus.APPLIED;
    }

    public Application(Long jobId, Long seekerId, Long resumeId, ApplicationStatus status) {
        this(null, jobId, seekerId, resumeId, status);
    }

    public Application(Long id, Long jobId, Long seekerId, Long resumeId, ApplicationStatus status) {
        this.id = id;
        this.jobId = jobId;
        this.seekerId = seekerId;
        this.resumeId = resumeId;
        this.status = status == null ? ApplicationStatus.APPLIED : status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getSeekerId() {
        return seekerId;
    }

    public void setSeekerId(Long seekerId) {
        this.seekerId = seekerId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    private java.sql.Timestamp appliedAt;
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String resumeFileName;

    public Application(Long id, Long jobId, Long seekerId, Long resumeId, ApplicationStatus status, java.sql.Timestamp appliedAt) {
        this.id = id;
        this.jobId = jobId;
        this.seekerId = seekerId;
        this.resumeId = resumeId;
        this.status = status == null ? ApplicationStatus.APPLIED : status;
        this.appliedAt = appliedAt;
    }

    public java.sql.Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(java.sql.Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    private String seekerName;
    private String seekerEmail;
    private String seekerPhone;

    public String getSeekerName() {
        return seekerName;
    }

    public void setSeekerName(String seekerName) {
        this.seekerName = seekerName;
    }

    public String getSeekerEmail() {
        return seekerEmail;
    }

    public void setSeekerEmail(String seekerEmail) {
        this.seekerEmail = seekerEmail;
    }

    public String getSeekerPhone() {
        return seekerPhone;
    }

    public void setSeekerPhone(String seekerPhone) {
        this.seekerPhone = seekerPhone;
    }

    @Override
    public String toString() {
        return "Application{id=" + id
                + ", jobId=" + jobId
                + ", seekerId=" + seekerId
                + ", resumeId=" + resumeId
                + ", status=" + status
                + ", appliedAt=" + appliedAt
                + '}';
    }
}
