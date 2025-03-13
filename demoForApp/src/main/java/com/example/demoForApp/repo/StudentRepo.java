package com.example.demoForApp.repo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demoForApp.entity.Student;
public interface StudentRepo extends JpaRepository<Student,Long>{
	
	// Search by name
    List<Student> findByStudentNameContainingIgnoreCase(String name);
    
    // Search by email
    List<Student> findByStudentEmailContainingIgnoreCase(String email);
    
    // Search by address
    List<Student> findByStudentAddressContainingIgnoreCase(String address);
}
