package com.taskmanagement.repository;

import com.taskmanagement.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    
    List<Board> findByInstituteIdAndIsArchivedFalse(Long instituteId);
    
    List<Board> findByCreatedById(Long userId);
    
    @Query("SELECT b FROM Board b JOIN b.members m WHERE m.id = :userId AND b.isArchived = false")
    List<Board> findBoardsByMemberId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Board b JOIN b.members m WHERE m.id = :userId AND b.isStarred = true")
    List<Board> findStarredBoardsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Board b WHERE b.institute.id = :instituteId")
    long countByInstituteId(@Param("instituteId") Long instituteId);
}
