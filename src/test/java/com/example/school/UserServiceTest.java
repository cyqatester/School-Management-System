package com.example.school;

import com.example.school.model.*;
import com.example.school.repo.JsonUserRepository;
import com.example.school.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    @TempDir
    Path dir;
    UserService service;

    @BeforeEach
    void setup() {
        service = new UserService(new JsonUserRepository(new ObjectMapper(), dir.resolve("users.json").toString()),
                new BCryptPasswordEncoder());
    }

    @Test
    void studentCanHaveMultipleTeachers() {
        User a = service.create("A", "One", "a", "password1", Role.TEACHER, null);
        User b = service.create("B", "Two", "b", "password2", Role.TEACHER, null);
        User s = service.create("S", "Kid", "s", "password3", Role.STUDENT, List.of(a.getId(), b.getId()));
        assertEquals(2, service.teachersFor(s).size());
    }

    @Test
    void duplicateUsernameRejected() {
        service.create("A", "One", "same", "password1", Role.STUDENT, null);
        assertThrows(IllegalArgumentException.class,
                () -> service.create("B", "Two", "SAME", "password2", Role.STUDENT, null));
    }

    @Test
    void superadminNameCannotBeDeleted() {
        User a = service.create("Super", "Admin", "superadmin", "password1", Role.ADMIN, null);
        assertThrows(IllegalArgumentException.class, () -> service.delete(a.getId()));
    }

    @Test
    void profileUpdatePreservesTeacherAssignments() {
        User t = service.create("T", "One", "teach", "password1", Role.TEACHER, null);
        User s = service.create("S", "Kid", "stud", "password1", Role.STUDENT, List.of(t.getId()));
        service.update(s.getId(), "New", "Kid", "stud", "", null);
        assertEquals(1, service.teachersFor(service.byId(s.getId()).orElseThrow()).size());
    }
}
