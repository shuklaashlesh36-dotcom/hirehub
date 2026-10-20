package com.hirehub.model;

public class Job {

    private Long id;
    private Long employerId;
    private String title;
    private String description;
    private String skills;
    private String location;
    private String salary;
    private JobStatus status;

    public Job() {
        this.status = JobStatus.PENDING;
    }

    public Job(Long employerId, String title, String description, String skills,
               String location, String salary, JobStatus status) {
        this(null, employerId, title, description, skills, location, salary, status);
    }

    public Job(Long id, Long employerId, String title, String description, String skills,
               String location, String salary, JobStatus status) {
        this.id = id;
        this.employerId = employerId;
        this.title = title;
        this.description = description;
        this.skills = skills;
        this.location = location;
        this.salary = salary;
        this.status = status == null ? JobStatus.PENDING : status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployerId() {
        return employerId;
    }

    public void setEmployerId(Long employerId) {
        this.employerId = employerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    private String companyName;
    private String jobType;
    private java.sql.Timestamp createdAt;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobType() {
        return jobType != null && !jobType.isBlank() ? jobType : "Full-time";
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Job{id=" + id
                + ", employerId=" + employerId
                + ", title='" + title + '\''
                + ", companyName='" + companyName + '\''
                + ", location='" + location + '\''
                + ", status=" + status
                + '}';
    }
}
