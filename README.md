# School Management System — demo

Spring Boot 4 MVC + Thymeleaf + Spring Security + Maven + JSON-file storage. Builds a WAR for external Apache Tomcat 11. Java 21. Spring Boot 4 requires the Servlet 6.1 baseline; use Tomcat 11.

## Run
`mvn clean spring-boot:run` then open http://localhost:8080

First run creates `superadmin` / `ChangeMe123!`. Change this immediately. Passwords are BCrypt hashes and are never displayed.

## JSON data
Default file: `./data/users.json`. Override with `SCHOOL_DATA_FILE`.
PowerShell:
`$env:SCHOOL_DATA_FILE = "C:\school-demo\users.json"`
`mvn spring-boot:run`

Keep this file outside the WAR/Tomcat deployment directory for persistence, and back it up.

## Build and deploy to Tomcat 11
`mvn clean verify`
WAR: `target/school-management-system.war`
Copy to `<TOMCAT_HOME>/webapps/` and start Tomcat 11. URL: `http://localhost:8080/school-management-system/`.

## Access
- Student: one dashboard, edit own name/username/password, view assigned teachers.
- Teacher: one dashboard, edit own profile, CRUD only students assigned to that teacher.
- Admin: one dashboard with Admins / Teachers / Students sub-tabs; CRUD users and assign multiple teachers to students. The original `superadmin` account cannot be deleted.
- All users have logout at the top-right.

## API
Session-authenticated read-only API:
- `GET /api/me`
- `GET /api/students` (admin: all; teacher: assigned only)
- `GET /api/teachers` (admin: all; student: assigned only)
- `GET /api/admin/users?role=ADMIN|TEACHER|STUDENT` (admin only)
API responses never include password hashes.

## Tests and CI
`mvn clean verify` runs JUnit 5 and REST Assured integration tests. GitHub Actions workflow: `.github/workflows/ci.yml`. It uploads test reports and the WAR.

## Demo-only warning
JSON storage is not suitable for multi-instance, high-concurrency or production school data. This project is a learning demo, not production-ready. Add HTTPS, backups, audit logs, password recovery, account lockout/rate limiting, data validation, and a transactional database before real deployment.
