# Task Management System - Complete Implementation Guide

This document contains all the remaining code needed to complete the Spring Boot Task Management System.

## Table of Contents
1. [Entity Classes](#entity-classes)
2. [Repository Interfaces](#repository-interfaces)
3. [Security Configuration](#security-configuration)
4. [Service Layer](#service-layer)
5. [Controller Layer](#controller-layer)
6. [WebSocket Configuration](#websocket-configuration)
7. [Frontend Templates](#frontend-templates)
8. [JavaScript Files](#javascript-files)
9. [CSS Styling](#css-styling)
10. [Database Setup](#database-setup)

---

## Entity Classes

### User.java
```java
package com.taskmanagement.entity;

import com.taskmanagement.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_institute", columnList = "institute_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_email", columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"institute", "createdBoards", "assignedTasks", "boardMemberships"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_id")
    private Institute institute;

    @Column(length = 50)
    private String phone;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Board> createdBoards = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo")
    private List<Task> assignedTasks = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private Set<Board> boardMemberships = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "board_favorites",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "board_id")
    )
    private Set<Board> favoriteBoards = new HashSet<>();

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
```

### Board.java
```java
package com.taskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "boards", indexes = {
    @Index(name = "idx_institute", columnList = "institute_id"),
    @Index(name = "idx_archived", columnList = "is_archived")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"institute", "createdBy", "members", "groups"})
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Board title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    @Column(name = "is_starred")
    private Boolean isStarred = false;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "cmps_order", columnDefinition = "JSON")
    private String cmpsOrder;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Group> groups = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "board_members",
        joinColumns = @JoinColumn(name = "board_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Group.java
```java
package com.taskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups", indexes = {
    @Index(name = "idx_board", columnList = "board_id"),
    @Index(name = "idx_position", columnList = "position")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"board", "tasks"})
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Group title is required")
    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(length = 7)
    private String color = "#0086c0";

    @Column(nullable = false)
    private Integer position = 0;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### Task.java
```java
package com.taskmanagement.entity;

import com.taskmanagement.enums.TaskPriority;
import com.taskmanagement.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_group", columnList = "group_id"),
    @Index(name = "idx_assigned", columnList = "assigned_to"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_due_date", columnList = "due_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"group", "assignedTo", "createdBy", "updatedBy", "comments", "attachments"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private Integer position = 0;

    @Column(name = "number_value", precision = 10, scale = 2)
    private BigDecimal numberValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Comment.java
```java
package com.taskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_task", columnList = "task_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"task", "user"})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Comment content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "font_weight", length = 20)
    private String fontWeight = "normal";

    @Column(name = "font_style", length = 20)
    private String fontStyle = "normal";

    @Column(name = "text_decoration", length = 20)
    private String textDecoration = "none";

    @Column(name = "text_align", length = 20)
    private String textAlign = "left";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### ActivityLog.java
```java
package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_board", columnList = "board_id"),
    @Index(name = "idx_task", columnList = "task_id"),
    @Index(name = "idx_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"board", "task", "group", "user"})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 100)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "from_value")
    private String fromValue;

    @Column(name = "to_value")
    private String toValue;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### Attachment.java
```java
package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments", indexes = {
    @Index(name = "idx_task", columnList = "task_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"task", "uploadedBy"})
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
```

---

## Repository Interfaces

Create these repository interfaces in `src/main/java/com/taskmanagement/repository/`:

### UserRepository.java
```java
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
}
```

### InstituteRepository.java
```java
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
```

### BoardRepository.java
```java
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
```

### GroupRepository.java
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

### TaskRepository.java
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

### CommentRepository.java
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

### ActivityLogRepository.java
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

### AttachmentRepository.java
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

---

## Continue in next response due to length...

The implementation guide continues with:
- Security Configuration (SecurityConfig.java, WebSocketConfig.java)
- Service Layer (all service classes with business logic)
- Controller Layer (all controllers)
- Thymeleaf Templates
- JavaScript files
- CSS styling
- Database initialization script

Would you like me to continue creating the remaining files, or would you prefer to follow this guide and implement the remaining components yourself?
