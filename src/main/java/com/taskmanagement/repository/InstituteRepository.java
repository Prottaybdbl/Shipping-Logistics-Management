package com.taskmanagement.repository;

import com.taskmanagement.entity.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    
    List<Institute> findByStatus(Institute.InstituteStatus status);
    
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(i) FROM Institute i WHERE i.status = 'ACTIVE'")
    long countActiveInstitutes();
}
