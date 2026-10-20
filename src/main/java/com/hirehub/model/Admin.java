package com.hirehub.model;

public class Admin extends User {

    public Admin(String name, String email, String password) {
        super(name, email, password, Role.ADMIN);
    }

    public Admin(Long id, String name, String email, String password) {
        super(id, name, email, password, Role.ADMIN);
    }

    @Override
    public String getDashboardType() {
        return "ADMIN_DASHBOARD";
    }
}
