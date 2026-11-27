package com.campus.placement.service;

import com.campus.placement.dao.JdbcStudentDao;
import com.campus.placement.dao.JdbcUserDao;
import com.campus.placement.dao.StudentDao;
import com.campus.placement.dao.UserDao;
import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.AuthenticationException;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.exception.DuplicateEntryException;
import com.campus.placement.exception.ValidationException;
import com.campus.placement.model.Role;
import com.campus.placement.model.Student;
import com.campus.placement.model.User;
import com.campus.placement.util.LoggerUtil;
import com.campus.placement.util.PasswordUtil;
import com.campus.placement.util.ValidationUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Handles authentication (salted PBKDF2 verification) and student self-registration.
 */
public class AuthService {

    private static final Logger LOGGER = LoggerUtil.getLogger(AuthService.class);

    private final UserDao userDao;
    private final StudentDao studentDao;
    private final AuditService auditService;

    public AuthService() {
        this(new JdbcUserDao(), new JdbcStudentDao(), new AuditService());
    }

    public AuthService(UserDao userDao, StudentDao studentDao, AuditService auditService) {
        this.userDao = userDao;
        this.studentDao = studentDao;
        this.auditService = auditService;
    }

    /**
     * Authenticates a user by email and plain-text password.
     *
     * @param email    user email
     * @param password plain-text password
     * @return authenticated {@link User}
     * @throws AuthenticationException if credentials are invalid or account is disabled/blocked
     * @throws DataAccessException     if DB query fails
     */
    public User login(String email, String password) throws AuthenticationException, DataAccessException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Email and password are required.");
        }

        Optional<User> optUser = userDao.findByEmail(email.trim());
        if (optUser.isEmpty()) {
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = optUser.get();
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            LOGGER.warning("Failed login attempt for email: " + email);
            throw new AuthenticationException("Invalid email or password.");
        }
        if (!user.isActive()) {
            throw new AuthenticationException("Account is deactivated. Contact the Placement Cell.");
        }

        if (user.getRole() == Role.STUDENT) {
            Optional<Student> optStudent = studentDao.findByUserId(user.getId());
            if (optStudent.isPresent() && optStudent.get().isBlocked()) {
                throw new AuthenticationException("Your student account is blocked by TPO.");
            }
        }

        auditService.log(user.getId(), "LOGIN", "USER", user.getId(), "Role=" + user.getRole());
        return user;
    }

    /**
     * Registers a new student account and profile inside a single JDBC transaction.
     *
     * @param student     student profile details
     * @param rawPassword plain-text password (hashed before storage)
     * @return persisted {@link Student}
     * @throws ValidationException     if any input is invalid
     * @throws DuplicateEntryException if email or roll number already exists
     * @throws DataAccessException     if persistence fails
     */
    public Student registerStudent(Student student, String rawPassword)
            throws ValidationException, DuplicateEntryException, DataAccessException {
        ValidationUtil.requireNonBlank(student.getRollNo(), "Roll Number");
        ValidationUtil.requireNonBlank(student.getFullName(), "Full Name");
        ValidationUtil.validateEmail(student.getEmail());
        ValidationUtil.validatePhone(student.getPhone());
        ValidationUtil.requireNonBlank(student.getBranch(), "Branch");
        ValidationUtil.validateCgpa(student.getCgpa());
        ValidationUtil.validateBacklogs(student.getActiveBacklogs());
        ValidationUtil.requireNonBlank(rawPassword, "Password");
        if (rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }

        if (userDao.findByEmail(student.getEmail().trim()).isPresent()
                || studentDao.findByEmail(student.getEmail().trim()).isPresent()) {
            throw new DuplicateEntryException("An account with this email already exists.");
        }
        if (studentDao.findByRollNo(student.getRollNo().trim()).isPresent()) {
            throw new DuplicateEntryException("A student with roll number " + student.getRollNo() + " already exists.");
        }

        String hash = PasswordUtil.hashPassword(rawPassword);

        return ConnectionManager.executeInTransaction(conn -> {
            int userId;
            String userSql = "INSERT INTO users (email, password_hash, role, active) VALUES (?, ?, 'STUDENT', 1)";
            try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, student.getEmail().trim().toLowerCase());
                ps.setString(2, hash);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    userId = keys.getInt(1);
                }
            }

            student.setUserId(userId);
            student.setEmail(student.getEmail().trim().toLowerCase());
            student.setRollNo(student.getRollNo().trim().toUpperCase());
            student.setBranch(student.getBranch().trim().toUpperCase());

            String stuSql = "INSERT INTO students (user_id, roll_no, full_name, email, phone, branch, "
                    + "batch_year, cgpa, active_backlogs, skills, resume_path, blocked) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
            try (PreparedStatement ps = conn.prepareStatement(stuSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, student.getRollNo());
                ps.setString(3, student.getFullName().trim());
                ps.setString(4, student.getEmail());
                ps.setString(5, student.getPhone().trim());
                ps.setString(6, student.getBranch());
                ps.setInt(7, student.getBatchYear());
                ps.setDouble(8, student.getCgpa());
                ps.setInt(9, student.getActiveBacklogs());
                ps.setString(10, student.getSkills());
                ps.setString(11, student.getResumePath());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        student.setId(keys.getInt(1));
                    }
                }
            }

            auditService.logInTransaction(conn, userId, "REGISTER", "STUDENT", student.getId(),
                    "Registered rollNo=" + student.getRollNo());
            return student;
        });
    }
}
