package com.example.demoForApp.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demoForApp.entity.Student;
import com.example.demoForApp.repo.StudentRepo;

@RestController
@RequestMapping("/api/students") // Base path for all student-related APIs
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentRepo studentRepo;

    // Create a new student
    @PostMapping
    public ResponseEntity<Student> saveStudent(@RequestBody Student student) {
        Student savedStudent = studentRepo.save(student);
        logger.info("New Student Added: {}", savedStudent);
        return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
    }

    // Get all students or filter by name, email, or address
    @GetMapping
    public ResponseEntity<List<Student>> getStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address) {

        List<Student> students;

        if (name != null) {
            students = studentRepo.findByStudentNameContainingIgnoreCase(name);
        } else if (email != null) {
            students = studentRepo.findByStudentEmailContainingIgnoreCase(email);
        } else if (address != null) {
            students = studentRepo.findByStudentAddressContainingIgnoreCase(address);
        } else {
            students = studentRepo.findAll();
        }

        if (students.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    // Get a student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable long id) {
        return studentRepo.findById(id)
                .map(student -> new ResponseEntity<>(student, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update student details
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable long id, @RequestBody Student updatedStudent) {
        return studentRepo.findById(id).map(student -> {
            student.setStudentName(updatedStudent.getStudentName());
            student.setStudentEmail(updatedStudent.getStudentEmail());
            student.setStudentAddress(updatedStudent.getStudentAddress());
            studentRepo.save(student);
            logger.info("Student Updated: {}", student);
            return new ResponseEntity<>(student, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete a student by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable long id) {
        return studentRepo.findById(id).map(student -> {
            studentRepo.deleteById(id);
            logger.info("Student Deleted: ID {}", id);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Batch Delete - Delete multiple students by ID list
    @DeleteMapping
    public ResponseEntity<Void> deleteStudents(@RequestParam List<Long> ids) {
        List<Student> students = studentRepo.findAllById(ids);
        if (students.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        studentRepo.deleteAllById(ids);
        logger.info("Batch Deletion Done for IDs: {}", ids);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Student Analytics (Total Students, Average Name Length, Email Percentage)
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getStudentAnalytics() {
        List<Student> students = studentRepo.findAll();

        if (students.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        int totalStudents = students.size();
        double avgNameLength = students.stream()
                                       .mapToInt(s -> s.getStudentName().length())
                                       .average()
                                       .orElse(0);

        long studentsWithEmail = students.stream()
                                         .filter(s -> s.getStudentEmail() != null && !s.getStudentEmail().isEmpty())
                                         .count();

        double emailPercentage = ((double) studentsWithEmail / totalStudents) * 100;

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalStudents", totalStudents);
        analytics.put("averageNameLength", avgNameLength);
        analytics.put("studentsWithEmail", studentsWithEmail);
        analytics.put("emailPercentage", emailPercentage);

        return new ResponseEntity<>(analytics, HttpStatus.OK);
    }
}
