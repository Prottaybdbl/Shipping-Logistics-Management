package com.taskmanagement.entity;

import com.taskmanagement.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entity representing a shipment cycle from mother vessel
 * This is the root of the hierarchical flow: Mother Vessel -> Lighters -> Trucks -> Products
 */
@Entity
@Table(name = "shipment_cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String consignee; // e.g., "PDL"

    @Column(nullable = false, name = "mother_vessel_name")
    private String motherVesselName; // e.g., "MEGHNA ENERGY"

    @Column(nullable = false, name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(nullable = false, name = "total_incoming_quantity")
    private Double totalIncomingQuantity;

    @Column(nullable = false, name = "item_type")
    private String itemType; // e.g., "10-20 Stone"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column(name = "shipment_document_path")
    private String shipmentDocumentPath; // File upload path

    @Column(name = "flow_summary", length = 1000)
    private String flowSummary; // Auto-generated: "Unloaded from 1 Mother Vessel to 3 Lighters, then to 12 Trucks"

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @OneToMany(mappedBy = "shipmentCycle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LighterLoading> lighterLoadings = new ArrayList<>();

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addLighterLoading(LighterLoading lighterLoading) {
        lighterLoadings.add(lighterLoading);
        lighterLoading.setShipmentCycle(this);
    }

    public void removeLighterLoading(LighterLoading lighterLoading) {
        lighterLoadings.remove(lighterLoading);
        lighterLoading.setShipmentCycle(null);
    }

    /**
     * Calculate total cost from all lighter loadings
     */
    public Double calculateTotalCost() {
        return lighterLoadings.stream()
                .mapToDouble(LighterLoading::calculateTotalCost)
                .sum();
    }

    /**
     * Calculate total loaded quantity across all lighters
     */
    public Double getTotalLoadedQuantity() {
        return lighterLoadings.stream()
                .mapToDouble(LighterLoading::getLoadedQuantity)
                .sum();
    }

    /**
     * Auto-generate flow summary
     */
    public void generateFlowSummary() {
        int lighterCount = lighterLoadings.size();
        int truckCount = lighterLoadings.stream()
                .mapToInt(lighter -> lighter.getTruckUnloadings().size())
                .sum();
        
        this.flowSummary = String.format(
            "Unloaded from 1 Mother Vessel (%s) to %d Lighter(s), then to %d Truck(s)",
            motherVesselName, lighterCount, truckCount
        );
    }
}
