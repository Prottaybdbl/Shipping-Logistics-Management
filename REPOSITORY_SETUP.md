# Repository Layer - Complete Code

Copy and create these files in `src/main/java/com/taskmanagement/repository/`:

## GroupRepository.java
```java
package com.taskmanagement.repository;

import com.taskmanagement.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    List<Group> findByBoardIdOrderByPositionAsc(Long boardId);
    
    @Query("SELECT MAX(g.position) FROM Group g WHERE g.board.id = :boardId")
    Integer findMaxPositionByBoardId(@Param("boardId") Long boardId);
}
```

## TaskRepository.java
```java
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
    List<Task> findByBoardIdAndAssignedToId(@Param("boardId") Long boardId, 
                                            @Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.group.board.id = :boardId AND " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Task> searchByBoardIdAndTitle(@Param("boardId") Long boardId, 
                                       @Param("title") String title);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.group.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.group.board.id = :boardId AND t.status = :status")
    long countByBoardIdAndStatus(@Param("boardId") Long boardId, @Param("status") TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("date") LocalDate date);
}
```

## CommentRepository.java
```java
package com.taskmanagement.repository;

import com.taskmanagement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    long countByTaskId(Long taskId);
}
```

## ActivityLogRepository.java
```java
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
```

## AttachmentRepository.java
```java
package com.taskmanagement.repository;

import com.taskmanagement.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    List<Attachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);
    
    long countByTaskId(Long taskId);
}
```
