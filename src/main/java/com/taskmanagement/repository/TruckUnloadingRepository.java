package com.taskmanagement.repository;

import com.taskmanagement.entity.TruckUnloading;
import com.taskmanagement.enums.LoadUnloadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TruckUnloadingRepository extends JpaRepository<TruckUnloading, Long> {

    List<TruckUnloading> findByLighterLoadingId(Long lighterLoadingId);

    List<TruckUnloading> findByLighterLoadingIdAndStatus(Long lighterLoadingId, LoadUnloadStatus status);

    @Query("SELECT tu FROM TruckUnloading tu " +
           "LEFT JOIN FETCH tu.productDetails " +
           "WHERE tu.id = :id")
    Optional<TruckUnloading> findByIdWithProductDetails(@Param("id") Long id);

    List<TruckUnloading> findByUnloadingDateBetween(LocalDate startDate, LocalDate endDate);

    List<TruckUnloading> findByDestinationContainingIgnoreCase(String destination);

    List<TruckUnloading> findByPartyContainingIgnoreCase(String party);

    @Query("SELECT tu FROM TruckUnloading tu " +
           "WHERE tu.lighterLoading.shipmentCycle.id = :shipmentCycleId")
    List<TruckUnloading> findByShipmentCycleId(@Param("shipmentCycleId") Long shipmentCycleId);

    @Query("SELECT SUM(tu.unloadedQuantity) FROM TruckUnloading tu WHERE tu.lighterLoading.id = :lighterLoadingId")
    Double sumUnloadedQuantityByLighter(@Param("lighterLoadingId") Long lighterLoadingId);

    @Query("SELECT COUNT(tu) FROM TruckUnloading tu WHERE tu.lighterLoading.id = :lighterLoadingId")
    Long countByLighterLoading(@Param("lighterLoadingId") Long lighterLoadingId);

    @Query("SELECT tu FROM TruckUnloading tu " +
           "WHERE tu.dependsOnLighterCompletion = true " +
           "AND tu.lighterLoading.status != 'LOADED'")
    List<TruckUnloading> findPendingDependentUnloadings();
}
