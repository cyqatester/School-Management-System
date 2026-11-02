package com.example.school.repo;

import com.example.school.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Repository
public class JsonUserRepository {
    private final ObjectMapper mapper;
    private final Path file;
    private final Map<String, User> users = new LinkedHashMap<>();

    public JsonUserRepository(ObjectMapper mapper, @Value("${school.data-file:./data/users.json}") String path) {
        this.mapper = mapper;
        file = Paths.get(path).toAbsolutePath().normalize();
        load();
    }

    private synchronized void load() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file)) {
                List<User> list = mapper.readValue(file.toFile(), new TypeReference<>() {
                });
                list.forEach(u -> users.put(u.getId(), u));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read user JSON", e);
        }
    }

    public synchronized List<User> all() {
        return users.values().stream().map(this::copy).toList();
    }

    public synchronized Optional<User> byId(String id) {
        return Optional.ofNullable(users.get(id)).map(this::copy);
    }

    public synchronized Optional<User> byUsername(String name) {
        return users.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(name)).findFirst().map(this::copy);
    }

    public synchronized boolean usernameExists(String name, String except) {
        return users.values().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(name) && !u.getId().equals(except));
    }

    public synchronized void save(User u) {
        users.put(u.getId(), copy(u));
        persist();
    }

    public synchronized void delete(String id) {
        users.remove(id);
        users.values().stream().filter(u -> u.getTeacherIds() != null).forEach(u -> u.getTeacherIds().remove(id));
        persist();
    }

    private void persist() {
        try {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), users.values());
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write user JSON", e);
        }
    }

    private User copy(User u) {
        return new User(u.getId(), u.getFirstName(), u.getLastName(), u.getUsername(), u.getPasswordHash(), u.getRole(),
                u.isProtectedAccount(), u.getTeacherIds());
    }
}
