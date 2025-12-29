package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByGroupIdOrderByPositionAsc(Long groupId);
    
    List<Task> findByAssignedToId(Long userId);
    
    List<Task> findByGroupBoardIdAndStatus(Long boardId, TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.group.board.id = :boardId AND t.assignedTo.id = :userId")
    List<Task> findByBoardIdAndAssignedToId(@Param("boardId") Long boardId, @Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.group.board.id = :boardId AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Task> searchByBoardIdAndTitle(@Param("boardId") Long boardId, @Param("title") String title);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.group.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.group.board.id = :boardId AND t.status = :status")
    long countByBoardIdAndStatus(@Param("boardId") Long boardId, @Param("status") TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("date") LocalDate date);
}
