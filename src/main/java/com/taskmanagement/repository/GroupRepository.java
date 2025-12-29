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
