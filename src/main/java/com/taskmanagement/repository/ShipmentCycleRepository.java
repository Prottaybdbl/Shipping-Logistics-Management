package com.taskmanagement.repository;

import com.taskmanagement.entity.ShipmentCycle;
import com.taskmanagement.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentCycleRepository extends JpaRepository<ShipmentCycle, Long> {

    List<ShipmentCycle> findByInstituteId(Long instituteId);

    List<ShipmentCycle> findByInstituteIdAndStatus(Long instituteId, ShipmentStatus status);

    List<ShipmentCycle> findByInstituteIdAndArrivalDateBetween(
            Long instituteId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM ShipmentCycle s " +
           "LEFT JOIN FETCH s.lighterLoadings " +
           "WHERE s.id = :id")
    Optional<ShipmentCycle> findByIdWithLighters(@Param("id") Long id);

    // NOTE: Avoid fetching multiple bag collections in one query to prevent
    // org.hibernate.loader.MultipleBagFetchException.
    // We fetch lighters here; truck unloadings will be loaded lazily as needed.
    @Query("SELECT s FROM ShipmentCycle s " +
           "LEFT JOIN FETCH s.lighterLoadings ll " +
           "WHERE s.id = :id")
    Optional<ShipmentCycle> findByIdWithFullHierarchy(@Param("id") Long id);

    List<ShipmentCycle> findByConsigneeContainingIgnoreCase(String consignee);

    List<ShipmentCycle> findByMotherVesselNameContainingIgnoreCase(String vesselName);

    @Query("SELECT s FROM ShipmentCycle s " +
           "WHERE s.institute.id = :instituteId " +
           "ORDER BY s.arrivalDate DESC")
    List<ShipmentCycle> findRecentByInstitute(@Param("instituteId") Long instituteId);

    @Query("SELECT COUNT(s) FROM ShipmentCycle s WHERE s.institute.id = :instituteId AND s.status = :status")
    Long countByInstituteAndStatus(@Param("instituteId") Long instituteId, @Param("status") ShipmentStatus status);

    @Query("SELECT SUM(s.totalIncomingQuantity) FROM ShipmentCycle s WHERE s.institute.id = :instituteId")
    Double sumTotalQuantityByInstitute(@Param("instituteId") Long instituteId);
}
