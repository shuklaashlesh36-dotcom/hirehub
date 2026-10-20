package com.hirehub.model;

/**
 * Common account data. Subclasses add role-specific fields and dashboard labels.
 */
public abstract class User implements DashboardAccessible {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;

    protected User(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    protected User(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private String profilePhoto;

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    /**
     * Role-specific home page label. Overridden by Admin, Employer, and JobSeeker.
     */
    @Override
    public abstract String getDashboardType();

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", role=" + role
                + '}';
    }
}
