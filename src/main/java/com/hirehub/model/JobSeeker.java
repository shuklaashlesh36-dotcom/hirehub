package com.hirehub.model;

public class JobSeeker extends User {

    private String phone;
    private String skills;
    private String education;

    public JobSeeker(String name, String email, String password,
                     String phone, String skills, String education) {
        super(name, email, password, Role.JOB_SEEKER);
        this.phone = phone;
        this.skills = skills;
        this.education = education;
    }

    public JobSeeker(Long id, String name, String email, String password,
                     String phone, String skills, String education) {
        super(id, name, email, password, Role.JOB_SEEKER);
        this.phone = phone;
        this.skills = skills;
        this.education = education;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    private String headline;

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    @Override
    public String getDashboardType() {
        return "JOB_SEEKER_DASHBOARD";
    }

    @Override
    public String toString() {
        return "JobSeeker{id=" + getId()
                + ", name='" + getName() + '\''
                + ", email='" + getEmail() + '\''
                + ", role=" + getRole()
                + ", education='" + education + '\''
                + '}';
    }
}
