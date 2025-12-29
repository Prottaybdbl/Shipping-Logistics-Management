package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for shipping dashboard analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingDashboardDTO {

    private SummaryStats summaryStats;
    private List<FlowVisualization> flowVisualizations;
    private List<CostBreakdown> costBreakdowns;
    private List<QuantityValidation> quantityValidations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryStats {
        private Long totalShipments;
        private Long pendingShipments;
        private Long inProgressShipments;
        private Long completedShipments;
        private Double totalIncomingQuantity;
        private Double totalDeliveredQuantity;
        private Double totalCost;
        private Integer totalLighters;
        private Integer totalTrucks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlowVisualization {
        private Long shipmentId;
        private String motherVesselName;
        private String consignee;
        private String flowSummary;
        private Integer lightersCount;
        private Integer trucksCount;
        private Map<String, Integer> lighterToTruckMap; // Lighter name -> truck count
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CostBreakdown {
        private String stage; // "Lighter", "Unloading", "Truck Transport"
        private Double totalCost;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuantityValidation {
        private Long shipmentId;
        private String motherVesselName;
        private Double incomingQuantity;
        private Double loadedQuantity;
        private Double unloadedQuantity;
        private Double deliveredQuantity;
        private Boolean isBalanced;
        private String validationMessage;
    }
}
