package com.taskmanagement.controller;

import com.taskmanagement.entity.Board;
import com.taskmanagement.entity.ShipmentEntry;
import com.taskmanagement.entity.User;
import com.taskmanagement.service.BoardService;
import com.taskmanagement.service.ShipmentEntryService;
import com.taskmanagement.service.WebSocketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/boards/{boardId}/entries")
public class ShipmentEntryApiController {

    private final ShipmentEntryService shipmentEntryService;
    private final BoardService boardService;
    private final WebSocketService webSocketService;

    public ShipmentEntryApiController(ShipmentEntryService shipmentEntryService, BoardService boardService, WebSocketService webSocketService) {
        this.shipmentEntryService = shipmentEntryService;
        this.boardService = boardService;
        this.webSocketService = webSocketService;
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@PathVariable Long boardId,
                                        @AuthenticationPrincipal User currentUser) {
        Board board = boardService.findById(boardId)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        // Check access
        if (!boardService.canUserAccessBoard(board, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        ShipmentEntry entry = new ShipmentEntry();
        entry.setBoard(board);
        entry.setCreatedBy(currentUser);
        entry.setUpdatedBy(currentUser);
        
        // Set position to be last
        Long count = shipmentEntryService.countByBoardId(boardId);
        entry.setPosition(count.intValue());
        
        ShipmentEntry saved = shipmentEntryService.save(entry);
        
        // Broadcast to all users viewing this board
        webSocketService.sendBoardUpdate(boardId, "ENTRY_CREATED", buildResponse(saved));
        
        return ResponseEntity.ok(buildResponse(saved));
    }

    @PatchMapping("/{entryId}")
    public ResponseEntity<?> updateEntry(@PathVariable Long boardId,
                                        @PathVariable Long entryId,
                                        @RequestBody Map<String, Object> updates,
                                        @AuthenticationPrincipal User currentUser) {
        Board board = boardService.findById(boardId)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        // Check access
        if (!boardService.canUserAccessBoard(board, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        ShipmentEntry entry = shipmentEntryService.findById(entryId)
            .orElseThrow(() -> new RuntimeException("Entry not found"));
        
        // Update fields dynamically
        updates.forEach((key, value) -> {
            try {
                updateField(entry, key, value);
            } catch (Exception e) {
                System.err.println("Error updating field " + key + ": " + e.getMessage());
            }
        });
        
        entry.setUpdatedBy(currentUser);
        ShipmentEntry saved = shipmentEntryService.save(entry);
        
        // Broadcast to all users viewing this board
        webSocketService.sendBoardUpdate(boardId, "ENTRY_UPDATED", buildResponse(saved));
        
        return ResponseEntity.ok(buildResponse(saved));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long boardId,
                                        @PathVariable Long entryId,
                                        @AuthenticationPrincipal User currentUser) {
        Board board = boardService.findById(boardId)
            .orElseThrow(() -> new RuntimeException("Board not found"));
        
        // Check access
        if (!boardService.canUserAccessBoard(board, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        shipmentEntryService.deleteById(entryId);
        
        // Broadcast to all users viewing this board
        webSocketService.sendBoardUpdate(boardId, "ENTRY_DELETED", Map.of("id", entryId));
        
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void updateField(ShipmentEntry entry, String field, Object value) {
        String stringValue = value != null ? value.toString() : null;
        
        switch (field) {
            // Loading Info
            case "consignee" -> entry.setConsignee(stringValue);
            case "lighterVesselName" -> entry.setLighterVesselName(stringValue);
            case "vesselDestination" -> entry.setVesselDestination(stringValue);
            case "date" -> entry.setDate(stringValue != null && !stringValue.isEmpty() ? LocalDate.parse(stringValue) : null);
            
            // Unloading & Transit Info
            case "challanNo" -> entry.setChallanNo(stringValue);
            case "convertingVessel" -> entry.setConvertingVessel(stringValue);
            case "noOfTrucks" -> entry.setNoOfTrucks(stringValue != null && !stringValue.isEmpty() ? Integer.parseInt(stringValue) : null);
            case "dischargingLocation" -> entry.setDischargingLocation(stringValue);
            case "finalDestination" -> entry.setFinalDestination(stringValue);
            
            // Product & Financials
            case "itemName" -> entry.setItemName(stringValue);
            case "billableQuantity" -> entry.setBillableQuantity(stringValue != null && !stringValue.isEmpty() ? new BigDecimal(stringValue) : null);
            case "lighterCost" -> entry.setLighterCost(stringValue != null && !stringValue.isEmpty() ? new BigDecimal(stringValue) : null);
            case "unloadCost" -> entry.setUnloadCost(stringValue != null && !stringValue.isEmpty() ? new BigDecimal(stringValue) : null);
            case "truckCost" -> entry.setTruckCost(stringValue != null && !stringValue.isEmpty() ? new BigDecimal(stringValue) : null);
        }
    }

    private Map<String, Object> buildResponse(ShipmentEntry entry) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", entry.getId());
        response.put("consignee", entry.getConsignee());
        response.put("lighterVesselName", entry.getLighterVesselName());
        response.put("vesselDestination", entry.getVesselDestination());
        response.put("date", entry.getDate());
        response.put("challanNo", entry.getChallanNo());
        response.put("convertingVessel", entry.getConvertingVessel());
        response.put("noOfTrucks", entry.getNoOfTrucks());
        response.put("dischargingLocation", entry.getDischargingLocation());
        response.put("finalDestination", entry.getFinalDestination());
        response.put("itemName", entry.getItemName());
        response.put("billableQuantity", entry.getBillableQuantity());
        response.put("lighterCost", entry.getLighterCost());
        response.put("unloadCost", entry.getUnloadCost());
        response.put("truckCost", entry.getTruckCost());
        response.put("totalUnitCosting", entry.getTotalUnitCosting());
        response.put("finalAmount", entry.getFinalAmount());
        return response;
    }
}
