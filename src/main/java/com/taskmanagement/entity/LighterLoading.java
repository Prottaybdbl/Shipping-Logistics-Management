package com.taskmanagement.entity;

import com.taskmanagement.enums.LoadUnloadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing loading onto lighters from mother vessel
 * Defines "unload from 1 (mother vessel) to many (lighters)" relationship
 */
@Entity
@Table(name = "lighter_loadings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LighterLoading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "lighter_name")
    private String lighterName; // e.g., "MV A&J Traders 04"

    @Column(name = "destination")
    private String destination; // e.g., "O/A - Tulatoli"

    @Column(name = "unloading_point")
    private String unloadingPoint; // e.g., "Tulatoli"

    @Column(nullable = false, name = "loading_date")
    private LocalDate loadingDate; // e.g., "29.10.25"

    @Column(nullable = false, name = "loaded_quantity")
    private Double loadedQuantity; // Portion of total incoming quantity

    @Column(name = "lighter_cost")
    private Double lighterCost = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadUnloadStatus status = LoadUnloadStatus.PENDING;

    @Column(name = "lighter_document_path")
    private String lighterDocumentPath; // File upload per lighter

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_cycle_id", nullable = false)
    private ShipmentCycle shipmentCycle;

    @OneToMany(mappedBy = "lighterLoading", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TruckUnloading> truckUnloadings = new ArrayList<>();

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addTruckUnloading(TruckUnloading truckUnloading) {
        truckUnloadings.add(truckUnloading);
        truckUnloading.setLighterLoading(this);
    }

    public void removeTruckUnloading(TruckUnloading truckUnloading) {
        truckUnloadings.remove(truckUnloading);
        truckUnloading.setLighterLoading(null);
    }

    /**
     * Calculate total cost including lighter cost and all truck unloading costs
     */
    public Double calculateTotalCost() {
        Double truckCosts = truckUnloadings.stream()
                .mapToDouble(TruckUnloading::calculateTotalCost)
                .sum();
        return (lighterCost != null ? lighterCost : 0.0) + truckCosts;
    }

    /**
     * Calculate total unloaded quantity from this lighter to all trucks
     */
    public Double getTotalUnloadedQuantity() {
        return truckUnloadings.stream()
                .mapToDouble(TruckUnloading::getUnloadedQuantity)
                .sum();
    }

    /**
     * Validate that unloaded quantity doesn't exceed loaded quantity
     */
    public boolean isQuantityBalanced() {
        return getTotalUnloadedQuantity() <= loadedQuantity;
    }

    /**
     * Get remaining quantity available for unloading
     */
    public Double getRemainingQuantity() {
        return loadedQuantity - getTotalUnloadedQuantity();
    }
}
