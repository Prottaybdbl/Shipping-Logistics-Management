package com.taskmanagement.repository;

import com.taskmanagement.entity.User;
import com.taskmanagement.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByInstituteId(Long instituteId);
    
    List<User> findByInstituteIdAndRole(Long instituteId, UserRole role);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.institute.id = :instituteId AND u.role IN :roles")
    List<User> findByInstituteIdAndRoles(@Param("instituteId") Long instituteId, 
                                         @Param("roles") List<UserRole> roles);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.institute.id = :instituteId")
    long countByInstituteId(@Param("instituteId") Long instituteId);
    
    @Query("SELECT u FROM User u WHERE u.isEnabled = true")
    List<User> findAllEnabled();
}
