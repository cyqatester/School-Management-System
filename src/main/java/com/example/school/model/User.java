package com.example.school.model;

import java.util.*;

public class User {
    private String id, firstName, lastName, username, passwordHash;
    private Role role;
    private boolean protectedAccount;
    private List<String> teacherIds = new ArrayList<>();

    public User() {
    }

    public User(String id, String firstName, String lastName, String username, String passwordHash, Role role,
            boolean protectedAccount, List<String> teacherIds) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.protectedAccount = protectedAccount;
        this.teacherIds = teacherIds == null ? new ArrayList<>() : new ArrayList<>(teacherIds);
    }

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String v) {
        firstName = v;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String v) {
        lastName = v;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        username = v;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String v) {
        passwordHash = v;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role v) {
        role = v;
    }

    public boolean isProtectedAccount() {
        return protectedAccount;
    }

    public void setProtectedAccount(boolean v) {
        protectedAccount = v;
    }

    public List<String> getTeacherIds() {
        return teacherIds;
    }

    public void setTeacherIds(List<String> v) {
        teacherIds = v == null ? new ArrayList<>() : new ArrayList<>(v);
    }
}
