package com.taskmanagement.service;

import com.taskmanagement.entity.ActivityLog;
import com.taskmanagement.entity.Board;
import com.taskmanagement.entity.Group;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.ActivityLogRepository;
import com.taskmanagement.repository.BoardRepository;
import com.taskmanagement.repository.GroupRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;

    public ActivityLog logActivity(String entityType, Long entityId, String action, 
                                   Long userId, String fromValue, String toValue, 
                                   String details, Board board, Task task, Group group) {
        ActivityLog log = new ActivityLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setFromValue(fromValue);
        log.setToValue(toValue);
        log.setDetails(details);

        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setUser);
        }
        if (board != null) {
            log.setBoard(board);
        }
        if (task != null) {
            log.setTask(task);
        }
        if (group != null) {
            log.setGroup(group);
        }

        return activityLogRepository.save(log);
    }

    public void logTaskCreated(Task task, Long userId) {
        logActivity("TASK", task.getId(), "create", userId, 
                null, null, "Task created: " + task.getTitle(), 
                task.getGroup().getBoard(), task, task.getGroup());
    }

    public void logTaskUpdated(Task task, Long userId, String field, String oldValue, String newValue) {
        logActivity("TASK", task.getId(), "update", userId, 
                oldValue, newValue, "Task " + field + " changed", 
                task.getGroup().getBoard(), task, task.getGroup());
    }

    public void logTaskAssigned(Task task, Long userId, String assigneeName) {
        logActivity("TASK", task.getId(), "assign", userId, 
                null, assigneeName, "Task assigned to " + assigneeName, 
                task.getGroup().getBoard(), task, task.getGroup());
    }

    public void logTaskDeleted(Long taskId, Long boardId, Long userId, String taskTitle) {
        Board board = boardRepository.findById(boardId).orElse(null);
        logActivity("TASK", taskId, "delete", userId, 
                null, null, "Task deleted: " + taskTitle, 
                board, null, null);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> getBoardActivities(Long boardId) {
        return activityLogRepository.findByBoardIdOrderByCreatedAtDesc(boardId);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> getTaskActivities(Long taskId) {
        return activityLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentBoardActivities(Long boardId) {
        return activityLogRepository.findRecentBoardActivities(boardId);
    }
}
