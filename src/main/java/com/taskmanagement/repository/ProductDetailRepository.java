package com.taskmanagement.repository;

import com.taskmanagement.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

    List<ProductDetail> findByTruckUnloadingId(Long truckUnloadingId);

    List<ProductDetail> findByItemContainingIgnoreCase(String item);

    @Query("SELECT pd FROM ProductDetail pd " +
           "WHERE pd.truckUnloading.lighterLoading.shipmentCycle.id = :shipmentCycleId")
    List<ProductDetail> findByShipmentCycleId(@Param("shipmentCycleId") Long shipmentCycleId);

    @Query("SELECT SUM(pd.deliveryQuantity) FROM ProductDetail pd " +
           "WHERE pd.truckUnloading.lighterLoading.shipmentCycle.id = :shipmentCycleId")
    Double sumDeliveryQuantityByShipment(@Param("shipmentCycleId") Long shipmentCycleId);

    @Query("SELECT pd.item, SUM(pd.deliveryQuantity), SUM(pd.lighterCost + pd.unloadingCost + pd.truckTransportCost) " +
           "FROM ProductDetail pd " +
           "WHERE pd.truckUnloading.lighterLoading.shipmentCycle.id = :shipmentCycleId " +
           "GROUP BY pd.item")
    List<Object[]> getProductSummaryByShipment(@Param("shipmentCycleId") Long shipmentCycleId);
}
