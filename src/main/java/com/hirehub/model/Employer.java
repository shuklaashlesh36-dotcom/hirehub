package com.hirehub.model;

public class Employer extends User {

    private String companyName;
    private String companyDescription;

    public Employer(String name, String email, String password, String companyName, String companyDescription) {
        super(name, email, password, Role.EMPLOYER);
        this.companyName = companyName;
        this.companyDescription = companyDescription;
    }

    public Employer(Long id, String name, String email, String password,
                    String companyName, String companyDescription) {
        super(id, name, email, password, Role.EMPLOYER);
        this.companyName = companyName;
        this.companyDescription = companyDescription;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    private String phone;
    private String website;
    private String location;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getDashboardType() {
        return "EMPLOYER_DASHBOARD";
    }

    @Override
    public String toString() {
        return "Employer{id=" + getId()
                + ", name='" + getName() + '\''
                + ", email='" + getEmail() + '\''
                + ", role=" + getRole()
                + ", companyName='" + companyName + '\''
                + '}';
    }
}
