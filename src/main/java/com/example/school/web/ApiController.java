package com.example.school.web;

import com.example.school.model.*;
import com.example.school.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserService users;

    public ApiController(UserService u) {
        users = u;
    }

    private User me(Authentication a) {
        return users.byUsername(a.getName()).orElseThrow();
    }

    @GetMapping("/me")
    public View meView(Authentication a) {
        return view(me(a));
    }

    @GetMapping("/students")
    public List<View> students(Authentication a) {
        User u = me(a);
        if (u.getRole() == Role.ADMIN)
            return users.byRole(Role.STUDENT).stream().map(this::view).toList();
        if (u.getRole() == Role.TEACHER)
            return users.studentsFor(u.getId()).stream().map(this::view).toList();
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/teachers")
    public List<View> teachers(Authentication a) {
        User u = me(a);
        if (u.getRole() == Role.ADMIN)
            return users.byRole(Role.TEACHER).stream().map(this::view).toList();
        if (u.getRole() == Role.STUDENT)
            return users.teachersFor(u).stream().map(this::view).toList();
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/admin/users")
    public List<View> admins(Authentication a, @RequestParam Role role) {
        if (me(a).getRole() != Role.ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return users.byRole(role).stream().map(this::view).toList();
    }

    private View view(User u) {
        return new View(u.getId(), u.getFirstName(), u.getLastName(), u.getUsername(), u.getRole(),
                u.isProtectedAccount(), u.getTeacherIds());
    }

    public record View(String id, String firstName, String lastName, String username, Role role,
            boolean protectedAccount, List<String> teacherIds) {
    }
}
