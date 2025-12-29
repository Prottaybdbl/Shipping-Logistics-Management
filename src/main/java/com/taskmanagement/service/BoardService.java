package com.taskmanagement.service;

import com.taskmanagement.entity.Board;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public Optional<Board> findById(Long id) {
        return boardRepository.findById(id);
    }

    public Board save(Board board) {
        return boardRepository.save(board);
    }

    public void deleteById(Long id) {
        boardRepository.deleteById(id);
    }

    public List<Board> findByCreatedBy(Long userId) {
        return boardRepository.findByCreatedById(userId);
    }

    public List<Board> findByMember(Long userId) {
        return boardRepository.findBoardsByMemberId(userId);
    }

    public boolean canUserAccessBoard(Board board, User user) {
        // Manager (creator) has full access
        if (board.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }
        // Officer: only if assigned as member
        return board.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
    }

    public void assignOfficer(Board board, User officer) {
        board.getMembers().add(officer);
        boardRepository.save(board);
    }

    public void removeOfficer(Board board, User officer) {
        board.getMembers().remove(officer);
        boardRepository.save(board);
    }
}
