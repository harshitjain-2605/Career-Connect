package com.campus.placement.model;

/**
 * Represents an authenticated account (Admin, Student, or Recruiter).
 */
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String email;
    private String passwordHash;
    private Role role;
    private Integer companyId;
    private boolean active = true;

    public User() {
    }

    public User(int id, String email, String passwordHash, Role role, Integer companyId, boolean active) {
        super(id);
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.companyId = companyId;
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
