package com.taskmanagement.repository;

import com.taskmanagement.entity.ShipmentEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentEntryRepository extends JpaRepository<ShipmentEntry, Long> {
    
    List<ShipmentEntry> findByBoardIdOrderByPositionAsc(Long boardId);
    
    boolean existsByChallanNo(String challanNo);
    
    Long countByBoardId(Long boardId);
}
