# Service Layer - Complete Implementation

## Custom UserDetailsService

Create `src/main/java/com/taskmanagement/service/CustomUserDetailsService.java`:

```java
package com.taskmanagement.service;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        if (!user.getIsEnabled()) {
            throw new UsernameNotFoundException("User account is disabled");
        }
        
        return user;
    }
}
```

## UserService

Create `src/main/java/com/taskmanagement/service/UserService.java`:

```java
package com.taskmanagement.service;

import com.taskmanagement.entity.Institute;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.UserRole;
import com.taskmanagement.repository.InstituteRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final InstituteRepository instituteRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String email, String password, String fullName, UserRole role, Long instituteId) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setIsEnabled(true);

        if (instituteId != null) {
            Institute institute = instituteRepository.findById(instituteId)
                    .orElseThrow(() -> new RuntimeException("Institute not found"));
            user.setInstitute(institute);
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public List<User> findByInstitute(Long instituteId) {
        return userRepository.findByInstituteId(instituteId);
    }

    @Transactional(readOnly = true)
    public List<User> findByInstituteAndRole(Long instituteId, UserRole role) {
        return userRepository.findByInstituteIdAndRole(instituteId, role);
    }

    public User updateUser(Long id, String fullName, String phone, String profileImage) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (fullName != null) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);
        if (profileImage != null) user.setProfileImage(profileImage);
        
        return userRepository.save(user);
    }

    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsEnabled(!user.getIsEnabled());
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
```

## InstituteService

Create `src/main/java/com/taskmanagement/service/InstituteService.java`:

```java
package com.taskmanagement.service;

import com.taskmanagement.entity.Institute;
import com.taskmanagement.repository.InstituteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InstituteService {

    private final InstituteRepository instituteRepository;

    public Institute createInstitute(String name, String address, String email, String phone) {
        if (instituteRepository.existsByName(name)) {
            throw new RuntimeException("Institute with name " + name + " already exists");
        }

        Institute institute = new Institute();
        institute.setName(name);
        institute.setAddress(address);
        institute.setEmail(email);
        institute.setPhone(phone);
        institute.setStatus(Institute.InstituteStatus.ACTIVE);

        return instituteRepository.save(institute);
    }

    @Transactional(readOnly = true)
    public List<Institute> findAll() {
        return instituteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Institute> findById(Long id) {
        return instituteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Institute> findActiveInstitutes() {
        return instituteRepository.findByStatus(Institute.InstituteStatus.ACTIVE);
    }

    public Institute updateInstitute(Long id, String name, String address, String email, String phone) {
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institute not found"));

        if (name != null) institute.setName(name);
        if (address != null) institute.setAddress(address);
        if (email != null) institute.setEmail(email);
        if (phone != null) institute.setPhone(phone);

        return instituteRepository.save(institute);
    }

    public void toggleInstituteStatus(Long id) {
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institute not found"));
        
        if (institute.getStatus() == Institute.InstituteStatus.ACTIVE) {
            institute.setStatus(Institute.InstituteStatus.INACTIVE);
        } else {
            institute.setStatus(Institute.InstituteStatus.ACTIVE);
        }
        
        instituteRepository.save(institute);
    }

    public void deleteInstitute(Long id) {
        instituteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countActiveInstitutes() {
        return instituteRepository.countActiveInstitutes();
    }
}
```

## BoardService

Create `src/main/java/com/taskmanagement/service/BoardService.java`:

```java
package com.taskmanagement.service;

import com.taskmanagement.entity.Board;
import com.taskmanagement.entity.Institute;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.BoardRepository;
import com.taskmanagement.repository.InstituteRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final InstituteRepository instituteRepository;
    private final ActivityService activityService;

    public Board createBoard(String title, String description, Long createdById, Long instituteId) {
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new RuntimeException("Institute not found"));

        Board board = new Board();
        board.setTitle(title);
        board.setDescription(description);
        board.setCreatedBy(creator);
        board.setInstitute(institute);
        board.setIsStarred(false);
        board.setIsArchived(false);
        board.setCmpsOrder("[\"status-picker\",\"member-picker\",\"date-picker\",\"priority-picker\"]");
        
        board.getMembers().add(creator);

        Board savedBoard = boardRepository.save(board);
        
        activityService.logActivity("BOARD", savedBoard.getId(), "create", createdById, 
                null, null, "Board created: " + title, savedBoard, null, null);

        return savedBoard;
    }

    @Transactional(readOnly = true)
    public Optional<Board> findById(Long id) {
        return boardRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Board> findByInstitute(Long instituteId) {
        return boardRepository.findByInstituteIdAndIsArchivedFalse(instituteId);
    }

    @Transactional(readOnly = true)
    public List<Board> findBoardsByUser(Long userId) {
        return boardRepository.findBoardsByMemberId(userId);
    }

    @Transactional(readOnly = true)
    public List<Board> findStarredBoards(Long userId) {
        return boardRepository.findStarredBoardsByUserId(userId);
    }

    public Board updateBoard(Long id, String title, String description, String cmpsOrder) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        if (title != null) board.setTitle(title);
        if (description != null) board.setDescription(description);
        if (cmpsOrder != null) board.setCmpsOrder(cmpsOrder);

        return boardRepository.save(board);
    }

    public void toggleStarred(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.setIsStarred(!board.getIsStarred());
        boardRepository.save(board);
    }

    public void archiveBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.setIsArchived(true);
        boardRepository.save(board);
    }

    public void addMember(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        board.getMembers().add(user);
        boardRepository.save(board);
    }

    public void removeMember(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        board.getMembers().remove(user);
        boardRepository.save(board);
    }

    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }
}
```

## Continue with TaskService, GroupService, CommentService, ActivityService, AttachmentService...

Due to length constraints, the remaining services (Task, Group, Comment, Activity, Attachment) follow similar patterns. 

Key points for remaining services:
- **TaskService**: CRUD operations for tasks, assign/unassign users, update status/priority/due date
- **GroupService**: CRUD for groups, reorder groups, change colors
- **CommentService**: CRUD for comments with rich text formatting
- **ActivityService**: Log all activities (create, update, delete, assign, etc.)
- **AttachmentService**: File upload, download, delete with file system storage

Each service should:
1. Use @Service annotation
2. Use @Transactional for write operations
3. Use @Transactional(readOnly = true) for read operations
4. Throw proper exceptions with meaningful messages
5. Log activities for audit trail

Would you like me to create the complete remaining service files, or shall I proceed to create the Controller layer and Templates?
