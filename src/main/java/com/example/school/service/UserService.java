package com.example.school.service;

import com.example.school.model.*;
import com.example.school.repo.JsonUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService implements CommandLineRunner {
    private final JsonUserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(JsonUserRepository r, PasswordEncoder e) {
        repo = r;
        encoder = e;
    }

    public void run(String... args) {
        if (repo.all().isEmpty())
            repo.save(new User(UUID.randomUUID().toString(), "Super", "Admin", "superadmin",
                    encoder.encode("ChangeMe123!"), Role.ADMIN, true, List.of()));
    }

    public List<User> all() {
        return repo.all();
    }

    public Optional<User> byId(String id) {
        return repo.byId(id);
    }

    public Optional<User> byUsername(String n) {
        return repo.byUsername(n);
    }

    public List<User> byRole(Role r) {
        return all().stream().filter(u -> u.getRole() == r).toList();
    }

    public List<User> studentsFor(String teacher) {
        return byRole(Role.STUDENT).stream().filter(s -> s.getTeacherIds().contains(teacher)).toList();
    }

    public List<User> teachersFor(User student) {
        Set<String> ids = new HashSet<>(student.getTeacherIds());
        return byRole(Role.TEACHER).stream().filter(t -> ids.contains(t.getId())).toList();
    }

    public User create(String first, String last, String username, String password, Role role,
            List<String> teacherIds) {
        validate(first, last, username, null);
        checkPassword(password);
        validateTeachers(role, teacherIds);
        User u = new User(UUID.randomUUID().toString(), first.trim(), last.trim(), username.trim(),
                encoder.encode(password), role, false, role == Role.STUDENT ? validIds(teacherIds) : List.of());
        repo.save(u);
        return u;
    }

    public User update(String id, String first, String last, String username, String password,
            List<String> teacherIds) {
        User u = repo.byId(id).orElseThrow(() -> new NoSuchElementException("User not found."));
        validate(first, last, username, id);
        u.setFirstName(first.trim());
        u.setLastName(last.trim());
        u.setUsername(username.trim());
        if (password != null && !password.isBlank()) {
            checkPassword(password);
            u.setPasswordHash(encoder.encode(password));
        }
        // null means preserve assignments (self-profile/teacher edits); empty list
        // means clear (admin edit).
        if (u.getRole() == Role.STUDENT && teacherIds != null) {
            validateTeachers(Role.STUDENT, teacherIds);
            u.setTeacherIds(validIds(teacherIds));
        }
        repo.save(u);
        return u;
    }

    public void delete(String id) {
        User u = repo.byId(id).orElseThrow();
        if (u.isProtectedAccount() || u.getUsername().equalsIgnoreCase("superadmin"))
            throw new IllegalArgumentException("The superadmin account cannot be deleted.");
        repo.delete(id);
    }

    private void validate(String f, String l, String n, String except) {
        if (f == null || f.isBlank() || l == null || l.isBlank() || n == null || n.isBlank())
            throw new IllegalArgumentException("First name, last name, and username are required.");
        if (repo.usernameExists(n.trim(), except))
            throw new IllegalArgumentException("Username already exists.");
    }

    private void checkPassword(String p) {
        if (p == null || p.length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters.");
    }

    private List<String> validIds(List<String> ids) {
        return ids == null ? new ArrayList<>() : ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void validateTeachers(Role role, List<String> ids) {
        if (role == Role.STUDENT && ids != null) {
            Set<String> valid = new HashSet<>(byRole(Role.TEACHER).stream().map(User::getId).toList());
            if (!valid.containsAll(ids))
                throw new IllegalArgumentException("Invalid teacher assignment.");
        }
    }
}
