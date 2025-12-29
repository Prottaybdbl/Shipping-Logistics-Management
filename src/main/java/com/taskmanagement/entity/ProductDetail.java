package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Product details per truck/unloading with granular cost breakdown
 */
@Entity
@Table(name = "product_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String item; // e.g., "10-20 Stone"

    @Column(name = "delivery_quantity")
    private Double deliveryQuantity; // e.g., 480, 350

    @Column(name = "survey_quantity")
    private Double surveyQuantity; // e.g., 100, 115

    @Column(name = "lighter_cost")
    private Double lighterCost = 0.0; // mirrored or apportioned

    @Column(name = "unloading_cost")
    private Double unloadingCost = 0.0; // mirrored from unloading

    @Column(name = "truck_transport_cost")
    private Double truckTransportCost = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_unloading_id", nullable = false)
    private TruckUnloading truckUnloading;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Double calculateTotalCost() {
        return (lighterCost != null ? lighterCost : 0.0)
                + (unloadingCost != null ? unloadingCost : 0.0)
                + (truckTransportCost != null ? truckTransportCost : 0.0);
    }
}
