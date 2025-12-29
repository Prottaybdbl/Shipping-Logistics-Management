package com.taskmanagement.controller;

import com.taskmanagement.entity.Board;
import com.taskmanagement.entity.ShipmentEntry;
import com.taskmanagement.entity.User;
import com.taskmanagement.service.BoardService;
import com.taskmanagement.service.ShipmentEntryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final BoardService boardService;
    private final ShipmentEntryService shipmentEntryService;

    public DashboardController(BoardService boardService, ShipmentEntryService shipmentEntryService) {
        this.boardService = boardService;
        this.shipmentEntryService = shipmentEntryService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User currentUser, Model model) {
        // Get user's boards
        List<Board> boards = boardService.findByCreatedBy(currentUser.getId());
        
        int totalBoards = boards.size();
        int totalShipments = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        Map<String, Integer> shipmentsPerBoard = new HashMap<>();
        Map<String, BigDecimal> revenuePerBoard = new HashMap<>();
        
        for (Board board : boards) {
            List<ShipmentEntry> entries = shipmentEntryService.findByBoardId(board.getId());
            int count = entries.size();
            totalShipments += count;
            
            BigDecimal boardRevenue = entries.stream()
                .map(ShipmentEntry::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalRevenue = totalRevenue.add(boardRevenue);
            
            shipmentsPerBoard.put(board.getTitle(), count);
            revenuePerBoard.put(board.getTitle(), boardRevenue);
        }
        
        model.addAttribute("totalBoards", totalBoards);
        model.addAttribute("totalShipments", totalShipments);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("shipmentsPerBoard", shipmentsPerBoard);
        model.addAttribute("revenuePerBoard", revenuePerBoard);
        model.addAttribute("boards", boards);
        
        return "dashboard/index";
    }
}
