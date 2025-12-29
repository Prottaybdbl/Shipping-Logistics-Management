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
