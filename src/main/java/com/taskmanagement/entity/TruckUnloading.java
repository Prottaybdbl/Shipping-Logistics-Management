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
 * Entity representing unloading from lighters to trucks
 * Defines "unload from many (lighters) to many (trucks)" relationship
 */
@Entity
@Table(name = "truck_unloadings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckUnloading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challan")
    private String challan; // Challan number

    @Column(nullable = false, name = "conveyance_name")
    private String conveyanceName; // e.g., "MV Innex 05" - should match lighter name

    @Column(name = "number_of_trucks")
    private Integer numberOfTrucks = 1; // e.g., "1 Truck", "2 Truck"

    @Column(name = "discharging_location")
    private String dischargingLocation; // e.g., "Tulatoli"

    @Column(name = "destination")
    private String destination; // e.g., "Motirhat", "Kaderpandithat"

    @Column(name = "party")
    private String party; // e.g., "PDL"

    @Column(nullable = false, name = "unloading_date")
    private LocalDate unloadingDate;

    @Column(nullable = false, name = "unloaded_quantity")
    private Double unloadedQuantity;

    @Column(name = "unloading_cost")
    private Double unloadingCost = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadUnloadStatus status = LoadUnloadStatus.PENDING;

    @Column(name = "depends_on_lighter_completion")
    private Boolean dependsOnLighterCompletion = true; // Dependency tracking

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lighter_loading_id", nullable = false)
    private LighterLoading lighterLoading;

    @OneToMany(mappedBy = "truckUnloading", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductDetail> productDetails = new ArrayList<>();

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addProductDetail(ProductDetail productDetail) {
        productDetails.add(productDetail);
        productDetail.setTruckUnloading(this);
    }

    public void removeProductDetail(ProductDetail productDetail) {
        productDetails.remove(productDetail);
        productDetail.setTruckUnloading(null);
    }

    /**
     * Calculate total cost including unloading cost and all product details costs
     */
    public Double calculateTotalCost() {
        Double productCosts = productDetails.stream()
                .mapToDouble(ProductDetail::calculateTotalCost)
                .sum();
        return (unloadingCost != null ? unloadingCost : 0.0) + productCosts;
    }

    /**
     * Get source lighter name for display/mirroring
     */
    public String getSourceLighterName() {
        return lighterLoading != null ? lighterLoading.getLighterName() : null;
    }

    /**
     * Check if this unloading can proceed based on lighter loading status
     */
    public boolean canProceed() {
        if (!dependsOnLighterCompletion) {
            return true;
        }
        return lighterLoading != null && 
               lighterLoading.getStatus() == LoadUnloadStatus.LOADED;
    }
}
