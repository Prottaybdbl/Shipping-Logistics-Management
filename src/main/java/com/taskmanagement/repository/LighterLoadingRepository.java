package com.taskmanagement.repository;

import com.taskmanagement.entity.LighterLoading;
import com.taskmanagement.enums.LoadUnloadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LighterLoadingRepository extends JpaRepository<LighterLoading, Long> {

    List<LighterLoading> findByShipmentCycleId(Long shipmentCycleId);

    List<LighterLoading> findByShipmentCycleIdAndStatus(Long shipmentCycleId, LoadUnloadStatus status);

    @Query("SELECT ll FROM LighterLoading ll " +
           "LEFT JOIN FETCH ll.truckUnloadings " +
           "WHERE ll.id = :id")
    Optional<LighterLoading> findByIdWithTruckUnloadings(@Param("id") Long id);

    List<LighterLoading> findByLighterNameContainingIgnoreCase(String lighterName);

    List<LighterLoading> findByLoadingDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT ll FROM LighterLoading ll WHERE ll.shipmentCycle.id = :shipmentCycleId " +
           "AND ll.loadedQuantity > (SELECT COALESCE(SUM(tu.unloadedQuantity), 0) FROM TruckUnloading tu WHERE tu.lighterLoading.id = ll.id)")
    List<LighterLoading> findWithRemainingCapacity(@Param("shipmentCycleId") Long shipmentCycleId);

    @Query("SELECT SUM(ll.loadedQuantity) FROM LighterLoading ll WHERE ll.shipmentCycle.id = :shipmentCycleId")
    Double sumLoadedQuantityByShipment(@Param("shipmentCycleId") Long shipmentCycleId);

    @Query("SELECT COUNT(ll) FROM LighterLoading ll WHERE ll.shipmentCycle.id = :shipmentCycleId")
    Long countByShipmentCycle(@Param("shipmentCycleId") Long shipmentCycleId);
}
