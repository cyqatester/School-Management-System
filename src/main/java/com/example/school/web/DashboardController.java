package com.example.school.web;

import com.example.school.model.*;
import com.example.school.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
public class DashboardController {
    private final UserService users;

    public DashboardController(UserService u) {
        users = u;
    }

    private User me(Authentication a) {
        return users.byUsername(a.getName()).orElseThrow();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model m) {
        User u = me(auth);
        m.addAttribute("me", u);
        m.addAttribute("profile", new Profile(u.getFirstName(), u.getLastName(), u.getUsername(), ""));
        if (u.getRole() == Role.STUDENT) {
            m.addAttribute("teachers", users.teachersFor(u));
            return "student";
        }
        if (u.getRole() == Role.TEACHER) {
            m.addAttribute("students", users.studentsFor(u.getId()));
            return "teacher";
        }
        m.addAttribute("admins", users.byRole(Role.ADMIN));
        m.addAttribute("teachers", users.byRole(Role.TEACHER));
        m.addAttribute("students", users.byRole(Role.STUDENT));
        m.addAttribute("allTeachers", users.byRole(Role.TEACHER));
        return "admin";
    }

    @PostMapping("/profile")
    public String profile(Authentication a, @ModelAttribute Profile f, RedirectAttributes r) {
        try {
            users.update(me(a).getId(), f.firstName, f.lastName, f.username, f.password, null);
            r.addFlashAttribute("success", "Profile saved.");
        } catch (RuntimeException e) {
            r.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/users")
    public String create(@ModelAttribute UserForm f, RedirectAttributes r) {
        try {
            users.create(f.firstName, f.lastName, f.username, f.password, f.role, f.teacherIds);
            r.addFlashAttribute("success", "Account created.");
        } catch (RuntimeException e) {
            r.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/users/{id}/update")
    public String adminUpdate(@PathVariable String id, @ModelAttribute UserForm f, RedirectAttributes r) {
        try {
            users.update(id, f.firstName, f.lastName, f.username, f.password,
                    f.teacherIds == null ? List.of() : f.teacherIds);
            r.addFlashAttribute("success", "Account updated.");
        } catch (RuntimeException e) {
            r.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String adminDelete(@PathVariable String id, RedirectAttributes r) {
        try {
            users.delete(id);
            r.addFlashAttribute("success", "Account deleted.");
        } catch (RuntimeException e) {
            r.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/teacher/students")
    public String teacherCreate(Authentication a, @ModelAttribute UserForm f, RedirectAttributes r) {
        try {
            users.create(f.firstName, f.lastName, f.username, f.password, Role.STUDENT, List.of(me(a).getId()));
            r.addFlashAttribute("success", "Student created.");
        } catch (RuntimeException e) {
            r.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/teacher/students/{id}/update")
    public String teacherUpdate(Authentication a, @PathVariable String id, @ModelAttribute UserForm f,
            RedirectAttributes r) {
        try {
            User t = me(a), s = users.byId(id).orElseThrow();
            if (s.getRole() != Role.STUDENT || !s.getTeacherIds().contains(t.getId()))
                throw new IllegalArgumentException("You may only edit students assigned to you.");
            users.update(id, f.firstName, f.lastName, f.username, f.password, null);
            r.addFlashAttribute("success", "Student updated.");
        } catch (RuntimeException e) {
            r.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/teacher/students/{id}/delete")
    public String teacherDelete(Authentication a, @PathVariable String id, RedirectAttributes r) {
        try {
            User t = me(a), s = users.byId(id).orElseThrow();
            if (s.getRole() != Role.STUDENT || !s.getTeacherIds().contains(t.getId()))
                throw new IllegalArgumentException("You may only delete students assigned to you.");
            users.delete(id);
            r.addFlashAttribute("success", "Student deleted.");
        } catch (RuntimeException e) {
            r.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    public static class Profile {
        public String firstName, lastName, username, password;

        public Profile() {
        }

        public Profile(String f, String l, String u, String p) {
            firstName = f;
            lastName = l;
            username = u;
            password = p;
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

        public String getPassword() {
            return password;
        }

        public void setPassword(String v) {
            password = v;
        }
    }

    public static class UserForm extends Profile {
        public Role role = Role.STUDENT;
        public List<String> teacherIds;

        public Role getRole() {
            return role;
        }

        public void setRole(Role v) {
            role = v;
        }

        public List<String> getTeacherIds() {
            return teacherIds;
        }

        public void setTeacherIds(List<String> v) {
            teacherIds = v;
        }
    }
}
