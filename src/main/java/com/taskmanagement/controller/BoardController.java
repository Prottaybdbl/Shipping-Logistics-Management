package com.taskmanagement.controller;

import com.taskmanagement.entity.Board;
import com.taskmanagement.entity.Institute;
import com.taskmanagement.entity.ShipmentEntry;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.UserRole;
import com.taskmanagement.service.BoardService;
import com.taskmanagement.service.ExcelExportService;
import com.taskmanagement.service.InstituteService;
import com.taskmanagement.service.ShipmentEntryService;
import com.taskmanagement.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class BoardController {

    private final BoardService boardService;
    private final ShipmentEntryService shipmentEntryService;
    private final UserService userService;
    private final InstituteService instituteService;
    private final ExcelExportService excelExportService;

    public BoardController(BoardService boardService, ShipmentEntryService shipmentEntryService, 
                          UserService userService, InstituteService instituteService, ExcelExportService excelExportService) {
        this.boardService = boardService;
        this.shipmentEntryService = shipmentEntryService;
        this.userService = userService;
        this.instituteService = instituteService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/boards")
    public String boardsPage(@AuthenticationPrincipal User currentUser, Model model) {
        // Admin cannot access boards
        if (currentUser.getRole() == UserRole.ADMIN) {
            return "redirect:/admin/dashboard?error=unauthorized";
        }
        
        List<Board> boards;
        
        // Manager/CEO sees boards they created
        if (currentUser.getRole() == UserRole.MANAGER || currentUser.getRole() == UserRole.CEO) {
            boards = boardService.findByCreatedBy(currentUser.getId());
        } else {
            // Officer sees only boards they're assigned to
            boards = boardService.findByMember(currentUser.getId());
        }
        
        model.addAttribute("boards", boards);
        model.addAttribute("currentUser", currentUser);
        return "board/board-list";
    }

    @GetMapping("/boards/new")
    public String newBoardForm(@AuthenticationPrincipal User currentUser, Model model) {
        // Only Manager and CEO can create boards
        if (currentUser.getRole() != UserRole.MANAGER && currentUser.getRole() != UserRole.CEO) {
            return "redirect:/boards?error=unauthorized";
        }
        
        model.addAttribute("board", new Board());
        return "board/board-form";
    }

    @PostMapping("/boards")
    public String createBoard(@AuthenticationPrincipal User currentUser, 
                             @ModelAttribute Board board, 
                             RedirectAttributes redirectAttributes) {
        if (currentUser.getRole() != UserRole.MANAGER && currentUser.getRole() != UserRole.CEO) {
            redirectAttributes.addFlashAttribute("error", "Only Manager and CEO can create boards");
            return "redirect:/boards";
        }
        
        board.setCreatedBy(currentUser);
        board.setInstitute(currentUser.getInstitute());
        boardService.save(board);
        redirectAttributes.addFlashAttribute("success", "Board created successfully");
        return "redirect:/boards/" + board.getId();
    }

    @GetMapping("/boards/{id}")
    public String viewBoard(@PathVariable Long id, 
                           @AuthenticationPrincipal User currentUser, 
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Board board = boardService.findById(id)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        // Check access permission
        if (!boardService.canUserAccessBoard(board, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "You don't have access to this board");
            return "redirect:/boards";
        }
        
        List<ShipmentEntry> entries = shipmentEntryService.findByBoardId(id);
        
        model.addAttribute("board", board);
        model.addAttribute("entries", entries);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isManager", board.getCreatedBy().getId().equals(currentUser.getId()));
        return "board/board-view";
    }

    @GetMapping("/boards/{id}/settings")
    public String boardSettings(@PathVariable Long id, 
                               @AuthenticationPrincipal User currentUser, 
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Board board = boardService.findById(id)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        // Only manager (creator) can access settings
        if (!board.getCreatedBy().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Only the board creator can access settings");
            return "redirect:/boards/" + id;
        }
        
        // Get officers from same institute
        List<User> availableOfficers = userService.findByInstituteId(currentUser.getInstitute().getId())
            .stream()
            .filter(u -> u.getRole() == UserRole.OFFICER)
            .toList();
        
        model.addAttribute("board", board);
        model.addAttribute("availableOfficers", availableOfficers);
        model.addAttribute("assignedOfficers", board.getMembers());
        return "board/board-settings";
    }

    @PostMapping("/boards/{id}/assign-officer")
    public String assignOfficer(@PathVariable Long id, 
                                @RequestParam Long officerId,
                                @AuthenticationPrincipal User currentUser,
                                RedirectAttributes redirectAttributes) {
        Board board = boardService.findById(id)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        if (!board.getCreatedBy().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized");
            return "redirect:/boards/" + id;
        }
        
        User officer = userService.findById(officerId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        boardService.assignOfficer(board, officer);
        redirectAttributes.addFlashAttribute("success", "Officer assigned successfully");
        return "redirect:/boards/" + id + "/settings";
    }

    @PostMapping("/boards/{id}/remove-officer")
    public String removeOfficer(@PathVariable Long id, 
                                @RequestParam Long officerId,
                                @AuthenticationPrincipal User currentUser,
                                RedirectAttributes redirectAttributes) {
        Board board = boardService.findById(id)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        if (!board.getCreatedBy().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized");
            return "redirect:/boards/" + id;
        }
        
        User officer = userService.findById(officerId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        boardService.removeOfficer(board, officer);
        redirectAttributes.addFlashAttribute("success", "Officer removed successfully");
        return "redirect:/boards/" + id + "/settings";
    }

    @PostMapping("/boards/{id}/delete")
    public String deleteBoard(@PathVariable Long id, 
                             @AuthenticationPrincipal User currentUser,
                             RedirectAttributes redirectAttributes) {
        Board board = boardService.findById(id)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        if (!board.getCreatedBy().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Only the board creator can delete it");
            return "redirect:/boards";
        }
        
        boardService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Board deleted successfully");
        return "redirect:/boards";
    }

    @GetMapping("/boards/{id}/export")
    public ResponseEntity<byte[]> exportBoardToExcel(@PathVariable Long id,
                                                     @AuthenticationPrincipal User currentUser) {
        Board board = boardService.findById(id)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        // Check access permission
        if (!boardService.canUserAccessBoard(board, currentUser)) {
            return ResponseEntity.status(403).build();
        }
        
        try {
            List<ShipmentEntry> entries = shipmentEntryService.findByBoardId(id);
            byte[] excelData = excelExportService.exportBoardToExcel(board, entries);
            
            String filename = board.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_") + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
