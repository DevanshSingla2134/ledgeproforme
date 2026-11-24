package com.ledgepro.model;

public class User extends BaseEntity {
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role; // ADMIN, OPERATOR

    public User() {
        super();
        this.role = "ADMIN";
    }

    public User(Long id, String username, String fullName, String email, String role) {
        super(id);
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
