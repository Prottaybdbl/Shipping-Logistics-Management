package com.taskmanagement.repository;

import com.taskmanagement.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    List<ActivityLog> findByBoardIdOrderByCreatedAtDesc(Long boardId);
    
    List<ActivityLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    @Query("SELECT a FROM ActivityLog a WHERE a.board.id = :boardId ORDER BY a.createdAt DESC LIMIT 50")
    List<ActivityLog> findRecentBoardActivities(@Param("boardId") Long boardId);
}
