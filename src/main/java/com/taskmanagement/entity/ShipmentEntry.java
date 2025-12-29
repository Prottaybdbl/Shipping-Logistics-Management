package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_entries", indexes = {
    @Index(name = "idx_board", columnList = "board_id"),
    @Index(name = "idx_challan", columnList = "challan_no"),
    @Index(name = "idx_date", columnList = "date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_challan_no", columnNames = "challan_no")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false)
    private Integer position = 0;

    // Group 1: Loading Info
    @Column(name = "consignee")
    private String consignee;

    @Column(name = "lighter_vessel_name")
    private String lighterVesselName;

    @Column(name = "vessel_destination")
    private String vesselDestination;

    @Column(name = "date")
    private LocalDate date;

    // Group 2: Unloading & Transit Info
    @Column(name = "challan_no", unique = true)
    private String challanNo;

    @Column(name = "converting_vessel")
    private String convertingVessel;

    @Column(name = "no_of_trucks")
    private Integer noOfTrucks;

    @Column(name = "discharging_location")
    private String dischargingLocation;

    @Column(name = "final_destination")
    private String finalDestination;

    // Group 3: Product & Financials
    @Column(name = "item_name")
    private String itemName;

    @Column(name = "billable_quantity", precision = 12, scale = 2)
    private BigDecimal billableQuantity;

    @Column(name = "lighter_cost", precision = 12, scale = 2)
    private BigDecimal lighterCost;

    @Column(name = "unload_cost", precision = 12, scale = 2)
    private BigDecimal unloadCost;

    @Column(name = "truck_cost", precision = 12, scale = 2)
    private BigDecimal truckCost;

    // Audit fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Calculated fields (not stored in DB, computed on-the-fly)
    @Transient
    public BigDecimal getTotalUnitCosting() {
        BigDecimal lighter = lighterCost != null ? lighterCost : BigDecimal.ZERO;
        BigDecimal unload = unloadCost != null ? unloadCost : BigDecimal.ZERO;
        BigDecimal truck = truckCost != null ? truckCost : BigDecimal.ZERO;
        return lighter.add(unload).add(truck);
    }

    @Transient
    public BigDecimal getFinalAmount() {
        BigDecimal quantity = billableQuantity != null ? billableQuantity : BigDecimal.ZERO;
        return quantity.multiply(getTotalUnitCosting());
    }
}
