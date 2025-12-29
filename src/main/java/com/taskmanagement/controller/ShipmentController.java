package com.taskmanagement.controller;

import com.taskmanagement.dto.ShipmentDTO;
import com.taskmanagement.dto.ShippingDashboardDTO;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final UserRepository userRepository;

    // ================== WEB PAGES ==================

    /**
     * Shipping Dashboard - Main view
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User currentUser, Model model) {
        Long instituteId = currentUser.getInstitute().getId();
        
        ShippingDashboardDTO dashboard = shipmentService.getDashboard(instituteId);
        List<ShipmentDTO> recentShipments = shipmentService.getAllShipmentsByInstitute(instituteId);
        
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("recentShipments", recentShipments);
        model.addAttribute("currentUser", currentUser);
        
        return "shipping/dashboard";
    }

    /**
     * View all shipments
     */
    @GetMapping("/shipments")
    public String listShipments(@AuthenticationPrincipal User currentUser, Model model) {
        Long instituteId = currentUser.getInstitute().getId();
        List<ShipmentDTO> shipments = shipmentService.getAllShipmentsByInstitute(instituteId);
        
        model.addAttribute("shipments", shipments);
        model.addAttribute("currentUser", currentUser);
        
        return "shipping/shipments";
    }

    /**
     * View single shipment with full hierarchy
     */
    @GetMapping("/shipment/{id}")
    public String viewShipment(@PathVariable Long id, Model model) {
        ShipmentDTO shipment = shipmentService.getShipment(id);
        Map<String, Object> validation = shipmentService.validateQuantities(id);
        
        model.addAttribute("shipment", shipment);
        model.addAttribute("validation", validation);
        
        return "shipping/shipment-detail";
    }

    /**
     * Create new shipment form
     */
    @GetMapping("/shipment/new")
    public String newShipmentForm(@AuthenticationPrincipal User currentUser, Model model) {
        ShipmentDTO shipmentDTO = new ShipmentDTO();
        shipmentDTO.setInstituteId(currentUser.getInstitute().getId());
        
        // Get officers from the same institute for assignment
        List<User> officers = userRepository.findByInstituteIdAndRole(
                currentUser.getInstitute().getId(), 
                com.taskmanagement.enums.UserRole.OFFICER
        );
        
        model.addAttribute("shipmentDTO", shipmentDTO);
        model.addAttribute("officers", officers);
        model.addAttribute("currentUser", currentUser);
        
        return "shipping/shipment-form";
    }

    /**
     * Create shipment (POST)
     */
    @PostMapping("/shipment/create")
    public String createShipment(
            @Valid @ModelAttribute("shipmentDTO") ShipmentDTO shipmentDTO,
            BindingResult result,
            @AuthenticationPrincipal User currentUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            // Re-add necessary model attributes for form rendering
            List<User> officers = userRepository.findByInstituteIdAndRole(
                    currentUser.getInstitute().getId(), 
                    com.taskmanagement.enums.UserRole.OFFICER
            );
            model.addAttribute("officers", officers);
            model.addAttribute("currentUser", currentUser);
            return "shipping/shipment-form";
        }
        
        try {
            ShipmentDTO created = shipmentService.createShipment(shipmentDTO, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Shipment created successfully: " + created.getMotherVesselName());
            return "redirect:/shipping/shipment/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating shipment: " + e.getMessage());
            return "redirect:/shipping/shipment/new";
        }
    }

    /**
     * Edit shipment form
     */
    @GetMapping("/shipment/{id}/edit")
    public String editShipmentForm(@PathVariable Long id, Model model) {
        ShipmentDTO shipment = shipmentService.getShipment(id);
        model.addAttribute("shipmentDTO", shipment);
        return "shipping/shipment-edit";
    }

    /**
     * Update shipment (POST)
     */
    @PostMapping("/shipment/{id}/update")
    public String updateShipment(
            @PathVariable Long id,
            @Valid @ModelAttribute("shipmentDTO") ShipmentDTO shipmentDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "shipping/shipment-edit";
        }
        
        try {
            shipmentService.updateShipment(id, shipmentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Shipment updated successfully");
            return "redirect:/shipping/shipment/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating shipment: " + e.getMessage());
            return "redirect:/shipping/shipment/" + id + "/edit";
        }
    }

    /**
     * Delete shipment
     */
    @PostMapping("/shipment/{id}/delete")
    public String deleteShipment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            shipmentService.deleteShipment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Shipment deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting shipment: " + e.getMessage());
        }
        return "redirect:/shipping/shipments";
    }

    // ================== REST API ENDPOINTS ==================

    /**
     * Get all shipments for institute (REST)
     */
    @GetMapping("/api/shipments")
    @ResponseBody
    public ResponseEntity<List<ShipmentDTO>> getShipments(@AuthenticationPrincipal User currentUser) {
        Long instituteId = currentUser.getInstitute().getId();
        List<ShipmentDTO> shipments = shipmentService.getAllShipmentsByInstitute(instituteId);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get single shipment (REST)
     */
    @GetMapping("/api/shipment/{id}")
    @ResponseBody
    public ResponseEntity<ShipmentDTO> getShipment(@PathVariable Long id) {
        ShipmentDTO shipment = shipmentService.getShipment(id);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Create shipment (REST)
     */
    @PostMapping("/api/shipment")
    @ResponseBody
    public ResponseEntity<ShipmentDTO> createShipmentApi(
            @Valid @RequestBody ShipmentDTO shipmentDTO,
            @AuthenticationPrincipal User currentUser) {
        
        ShipmentDTO created = shipmentService.createShipment(shipmentDTO, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update shipment (REST)
     */
    @PutMapping("/api/shipment/{id}")
    @ResponseBody
    public ResponseEntity<ShipmentDTO> updateShipmentApi(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentDTO shipmentDTO) {
        
        ShipmentDTO updated = shipmentService.updateShipment(id, shipmentDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Add lighter to shipment (REST)
     */
    @PostMapping("/api/shipment/{shipmentId}/lighter")
    @ResponseBody
    public ResponseEntity<ShipmentDTO> addLighter(
            @PathVariable Long shipmentId,
            @Valid @RequestBody ShipmentDTO.LighterLoadingDTO lighterDTO) {
        
        ShipmentDTO updated = shipmentService.addLighterLoading(shipmentId, lighterDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Add truck to lighter (REST)
     */
    @PostMapping("/api/lighter/{lighterId}/truck")
    @ResponseBody
    public ResponseEntity<ShipmentDTO.LighterLoadingDTO> addTruck(
            @PathVariable Long lighterId,
            @Valid @RequestBody ShipmentDTO.TruckUnloadingDTO truckDTO) {
        
        ShipmentDTO.LighterLoadingDTO updated = shipmentService.addTruckUnloading(lighterId, truckDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Validate quantities (REST)
     */
    @GetMapping("/api/shipment/{id}/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateQuantities(@PathVariable Long id) {
        Map<String, Object> validation = shipmentService.validateQuantities(id);
        return ResponseEntity.ok(validation);
    }

    /**
     * Get dashboard analytics (REST)
     */
    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<ShippingDashboardDTO> getDashboardApi(@AuthenticationPrincipal User currentUser) {
        Long instituteId = currentUser.getInstitute().getId();
        ShippingDashboardDTO dashboard = shipmentService.getDashboard(instituteId);
        return ResponseEntity.ok(dashboard);
    }

    // ================== EXCEPTION HANDLER ==================

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}
