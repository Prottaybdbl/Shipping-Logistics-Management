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
