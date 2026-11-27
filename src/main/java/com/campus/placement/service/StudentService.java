package com.campus.placement.service;

import com.campus.placement.config.AppConfig;
import com.campus.placement.dao.JdbcStudentDao;
import com.campus.placement.dao.StudentDao;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.exception.ValidationException;
import com.campus.placement.model.Student;
import com.campus.placement.util.FileUtil;
import com.campus.placement.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for student profile management, search/sort, resume upload, and blocking.
 */
// [Unit 2: Comparator]
public class StudentService {

    private final StudentDao studentDao;
    private final AuditService auditService;

    public StudentService() {
        this(new JdbcStudentDao(), new AuditService());
    }

    public StudentService(StudentDao studentDao, AuditService auditService) {
        this.studentDao = studentDao;
        this.auditService = auditService;
    }

    public Optional<Student> getByUserId(int userId) throws DataAccessException {
        return studentDao.findByUserId(userId);
    }

    public Optional<Student> getById(int studentId) throws DataAccessException {
        return studentDao.findById(studentId);
    }

    public List<Student> getAllStudents() throws DataAccessException {
        return studentDao.findAll();
    }

    /**
     * Searches students and applies optional sorting via {@link Comparator}.
     */
    public List<Student> searchStudents(String branch, double minCgpa, String keyword, String sortBy)
            throws DataAccessException {
        List<Student> list = studentDao.search(branch, minCgpa, keyword);
        if ("CGPA_DESC".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparingDouble(Student::getCgpa).reversed());
        } else if ("NAME_ASC".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER));
        } else if ("ROLL_ASC".equalsIgnoreCase(sortBy)) {
            list.sort(Comparator.comparing(Student::getRollNo));
        }
        return list;
    }

    public void updateStudent(Student student, Integer actorUserId)
            throws ValidationException, DataAccessException {
        ValidationUtil.requireNonBlank(student.getFullName(), "Full Name");
        ValidationUtil.validatePhone(student.getPhone());
        ValidationUtil.requireNonBlank(student.getBranch(), "Branch");
        ValidationUtil.validateCgpa(student.getCgpa());
        ValidationUtil.validateBacklogs(student.getActiveBacklogs());

        studentDao.update(student);
        auditService.log(actorUserId, "UPDATE", "STUDENT", student.getId(),
                "Updated profile rollNo=" + student.getRollNo());
    }

    /**
     * Copies a resume file into the app-managed {@code resumes/} folder and stores the path.
     */
    public String uploadResume(Student student, File sourceFile, Integer actorUserId)
            throws ValidationException, DataAccessException {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new ValidationException("Please select a valid resume file.");
        }
        try {
            String storedPath = FileUtil.copyResume(sourceFile, AppConfig.getInstance().getResumesDir(),
                    student.getRollNo());
            student.setResumePath(storedPath);
            studentDao.update(student);
            auditService.log(actorUserId, "UPLOAD_RESUME", "STUDENT", student.getId(),
                    "Uploaded resume: " + storedPath);
            return storedPath;
        } catch (IOException e) {
            throw new DataAccessException("Failed to copy resume file: " + e.getMessage(), e);
        }
    }

    public void setBlocked(int studentId, boolean blocked, Integer actorUserId) throws DataAccessException {
        studentDao.setBlocked(studentId, blocked);
        auditService.log(actorUserId, blocked ? "BLOCK" : "UNBLOCK", "STUDENT", studentId,
                "blocked=" + blocked);
    }

    public void deleteStudent(int studentId, Integer actorUserId) throws DataAccessException {
        studentDao.deleteById(studentId);
        auditService.log(actorUserId, "DELETE", "STUDENT", studentId, "Deleted student id=" + studentId);
    }
}
