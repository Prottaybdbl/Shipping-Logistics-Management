package com.taskmanagement.dto;

import com.taskmanagement.enums.LoadUnloadStatus;
import com.taskmanagement.enums.ShipmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating and updating shipment cycles with full hierarchy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDTO {

    private Long id;

    @NotBlank(message = "Consignee is required")
    private String consignee;

    @NotBlank(message = "Mother vessel name is required")
    private String motherVesselName;

    @NotNull(message = "Arrival date is required")
    private LocalDate arrivalDate;

    @NotNull(message = "Total incoming quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double totalIncomingQuantity;

    @NotBlank(message = "Item type is required")
    private String itemType;

    private ShipmentStatus status;
    private String shipmentDocumentPath;
    private String flowSummary;

    // Set from the authenticated user's institute in the controller; not user-editable
    private Long instituteId;

    private Long assignedToUserId; // Officer assigned to this shipment

    @Builder.Default
    private List<LighterLoadingDTO> lighterLoadings = new ArrayList<>();

    // Response fields
    private Double totalCost;
    private String assignedToName; // For display
    private Double totalLoadedQuantity;
    private Integer lighterCount;
    private Integer truckCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LighterLoadingDTO {
        private Long id;

        @NotBlank(message = "Lighter name is required")
        private String lighterName;

        private String destination;
        private String unloadingPoint;

        @NotNull(message = "Loading date is required")
        private LocalDate loadingDate;

        @NotNull(message = "Loaded quantity is required")
        @Positive(message = "Quantity must be positive")
        private Double loadedQuantity;

        private Double lighterCost;
        private LoadUnloadStatus status;
        private String lighterDocumentPath;

        @Valid
        @Builder.Default
        private List<TruckUnloadingDTO> truckUnloadings = new ArrayList<>();

        // Response fields
        private Double totalCost;
        private Double totalUnloadedQuantity;
        private Double remainingQuantity;
        private Boolean isBalanced;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TruckUnloadingDTO {
        private Long id;
        private String challan;

        @NotBlank(message = "Conveyance name is required")
        private String conveyanceName;

        private Integer numberOfTrucks;
        private String dischargingLocation;
        private String destination;
        private String party;

        @NotNull(message = "Unloading date is required")
        private LocalDate unloadingDate;

        @NotNull(message = "Unloaded quantity is required")
        @Positive(message = "Quantity must be positive")
        private Double unloadedQuantity;

        private Double unloadingCost;
        private LoadUnloadStatus status;
        private Boolean dependsOnLighterCompletion;

        @Valid
        @Builder.Default
        private List<ProductDetailDTO> productDetails = new ArrayList<>();

        // Response fields
        private String sourceLighterName;
        private Double totalCost;
        private Boolean canProceed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductDetailDTO {
        private Long id;

        @NotBlank(message = "Item name is required")
        private String item;

        private Double deliveryQuantity;
        private Double surveyQuantity;
        private Double lighterCost;
        private Double unloadingCost;
        private Double truckTransportCost;

        // Response field
        private Double totalCost;
    }
}
