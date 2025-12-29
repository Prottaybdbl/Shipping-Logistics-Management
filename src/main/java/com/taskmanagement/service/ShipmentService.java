package com.taskmanagement.service;

import com.taskmanagement.dto.ShipmentDTO;
import com.taskmanagement.dto.ShippingDashboardDTO;
import com.taskmanagement.entity.*;
import com.taskmanagement.enums.ShipmentStatus;
import com.taskmanagement.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentCycleRepository shipmentCycleRepository;
    private final LighterLoadingRepository lighterLoadingRepository;
    private final TruckUnloadingRepository truckUnloadingRepository;
    private final ProductDetailRepository productDetailRepository;
    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShipmentDTO createShipment(ShipmentDTO dto, Long userId) {
        Institute institute = instituteRepository.findById(dto.getInstituteId())
                .orElseThrow(() -> new RuntimeException("Institute not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get assigned officer if provided
        User assignedOfficer = null;
        if (dto.getAssignedToUserId() != null) {
            assignedOfficer = userRepository.findById(dto.getAssignedToUserId())
                    .orElse(null);
        }

        ShipmentCycle shipment = ShipmentCycle.builder()
                .consignee(dto.getConsignee())
                .motherVesselName(dto.getMotherVesselName())
                .arrivalDate(dto.getArrivalDate())
                .totalIncomingQuantity(dto.getTotalIncomingQuantity())
                .itemType(dto.getItemType())
                .status(ShipmentStatus.PENDING)
                .shipmentDocumentPath(dto.getShipmentDocumentPath())
                .institute(institute)
                .createdBy(user)
                .assignedTo(assignedOfficer)
                .build();

        // Add lighter loadings
        for (ShipmentDTO.LighterLoadingDTO lighterDTO : dto.getLighterLoadings()) {
            LighterLoading lighter = mapToLighterEntity(lighterDTO);
            shipment.addLighterLoading(lighter);

            // Add truck unloadings
            for (ShipmentDTO.TruckUnloadingDTO truckDTO : lighterDTO.getTruckUnloadings()) {
                TruckUnloading truck = mapToTruckEntity(truckDTO);
                lighter.addTruckUnloading(truck);

                // Add product details
                for (ShipmentDTO.ProductDetailDTO productDTO : truckDTO.getProductDetails()) {
                    ProductDetail product = mapToProductEntity(productDTO);
                    truck.addProductDetail(product);
                }
            }
        }

        shipment.generateFlowSummary();
        shipment = shipmentCycleRepository.save(shipment);

        return mapToDTO(shipment);
    }

    @Transactional
    public ShipmentDTO updateShipment(Long shipmentId, ShipmentDTO dto) {
        ShipmentCycle shipment = shipmentCycleRepository.findByIdWithFullHierarchy(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        shipment.setConsignee(dto.getConsignee());
        shipment.setMotherVesselName(dto.getMotherVesselName());
        shipment.setArrivalDate(dto.getArrivalDate());
        shipment.setTotalIncomingQuantity(dto.getTotalIncomingQuantity());
        shipment.setItemType(dto.getItemType());
        if (dto.getStatus() != null) {
            shipment.setStatus(dto.getStatus());
        }

        shipment.generateFlowSummary();
        shipment = shipmentCycleRepository.save(shipment);

        return mapToDTO(shipment);
    }

    public ShipmentDTO getShipment(Long shipmentId) {
        ShipmentCycle shipment = shipmentCycleRepository.findByIdWithFullHierarchy(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        return mapToDTO(shipment);
    }

    public List<ShipmentDTO> getAllShipmentsByInstitute(Long instituteId) {
        List<ShipmentCycle> shipments = shipmentCycleRepository.findByInstituteId(instituteId);
        return shipments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteShipment(Long shipmentId) {
        shipmentCycleRepository.deleteById(shipmentId);
    }

    /**
     * Add lighter loading to existing shipment
     */
    @Transactional
    public ShipmentDTO addLighterLoading(Long shipmentId, ShipmentDTO.LighterLoadingDTO lighterDTO) {
        ShipmentCycle shipment = shipmentCycleRepository.findByIdWithLighters(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        LighterLoading lighter = mapToLighterEntity(lighterDTO);
        shipment.addLighterLoading(lighter);

        // Validate total loaded quantity doesn't exceed incoming
        validateLoadedQuantity(shipment);

        shipment.generateFlowSummary();
        shipment = shipmentCycleRepository.save(shipment);

        return mapToDTO(shipment);
    }

    /**
     * Add truck unloading to existing lighter
     */
    @Transactional
    public ShipmentDTO.LighterLoadingDTO addTruckUnloading(Long lighterId, ShipmentDTO.TruckUnloadingDTO truckDTO) {
        LighterLoading lighter = lighterLoadingRepository.findByIdWithTruckUnloadings(lighterId)
                .orElseThrow(() -> new RuntimeException("Lighter not found"));

        TruckUnloading truck = mapToTruckEntity(truckDTO);
        lighter.addTruckUnloading(truck);

        // Validate unloaded quantity doesn't exceed loaded
        if (!lighter.isQuantityBalanced()) {
            throw new RuntimeException("Unloaded quantity exceeds loaded quantity for lighter: " + lighter.getLighterName());
        }

        lighter = lighterLoadingRepository.save(lighter);

        // Update shipment flow summary
        ShipmentCycle shipment = lighter.getShipmentCycle();
        shipment.generateFlowSummary();
        shipmentCycleRepository.save(shipment);

        return mapToLighterDTO(lighter);
    }

    /**
     * Validate quantities across the flow
     */
    public Map<String, Object> validateQuantities(Long shipmentId) {
        ShipmentCycle shipment = shipmentCycleRepository.findByIdWithFullHierarchy(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        Map<String, Object> validation = new HashMap<>();
        validation.put("shipmentId", shipmentId);
        validation.put("motherVessel", shipment.getMotherVesselName());
        validation.put("incomingQuantity", shipment.getTotalIncomingQuantity());
        validation.put("totalLoadedQuantity", shipment.getTotalLoadedQuantity());

        boolean isBalanced = shipment.getTotalLoadedQuantity() <= shipment.getTotalIncomingQuantity();
        validation.put("isBalanced", isBalanced);

        if (!isBalanced) {
            validation.put("message", "WARNING: Loaded quantity exceeds incoming quantity!");
        }

        // Check each lighter
        List<Map<String, Object>> lighterValidations = new ArrayList<>();
        for (LighterLoading lighter : shipment.getLighterLoadings()) {
            Map<String, Object> lighterVal = new HashMap<>();
            lighterVal.put("lighterName", lighter.getLighterName());
            lighterVal.put("loadedQuantity", lighter.getLoadedQuantity());
            lighterVal.put("totalUnloadedQuantity", lighter.getTotalUnloadedQuantity());
            lighterVal.put("remainingQuantity", lighter.getRemainingQuantity());
            lighterVal.put("isBalanced", lighter.isQuantityBalanced());

            if (!lighter.isQuantityBalanced()) {
                lighterVal.put("message", "WARNING: Unloaded quantity exceeds loaded quantity!");
            }

            lighterValidations.add(lighterVal);
        }
        validation.put("lighterValidations", lighterValidations);

        return validation;
    }

    /**
     * Get dashboard analytics
     */
    public ShippingDashboardDTO getDashboard(Long instituteId) {
        List<ShipmentCycle> shipments = shipmentCycleRepository.findByInstituteId(instituteId);

        // Summary stats
        ShippingDashboardDTO.SummaryStats stats = ShippingDashboardDTO.SummaryStats.builder()
                .totalShipments((long) shipments.size())
                .pendingShipments(shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.PENDING).count())
                .inProgressShipments(shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.IN_PROGRESS).count())
                .completedShipments(shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.COMPLETED).count())
                .totalIncomingQuantity(shipments.stream().mapToDouble(ShipmentCycle::getTotalIncomingQuantity).sum())
                .totalCost(shipments.stream().mapToDouble(ShipmentCycle::calculateTotalCost).sum())
                .totalLighters((int) shipments.stream().mapToLong(s -> s.getLighterLoadings().size()).sum())
                .totalTrucks((int) shipments.stream()
                        .flatMap(s -> s.getLighterLoadings().stream())
                        .mapToLong(l -> l.getTruckUnloadings().size()).sum())
                .build();

        // Flow visualizations
        List<ShippingDashboardDTO.FlowVisualization> flows = shipments.stream()
                .map(s -> {
                    Map<String, Integer> lighterToTruck = s.getLighterLoadings().stream()
                            .collect(Collectors.toMap(
                                    LighterLoading::getLighterName,
                                    l -> l.getTruckUnloadings().size()
                            ));

                    return ShippingDashboardDTO.FlowVisualization.builder()
                            .shipmentId(s.getId())
                            .motherVesselName(s.getMotherVesselName())
                            .consignee(s.getConsignee())
                            .flowSummary(s.getFlowSummary())
                            .lightersCount(s.getLighterLoadings().size())
                            .trucksCount((int) s.getLighterLoadings().stream()
                                    .mapToLong(l -> l.getTruckUnloadings().size()).sum())
                            .lighterToTruckMap(lighterToTruck)
                            .build();
                })
                .collect(Collectors.toList());

        return ShippingDashboardDTO.builder()
                .summaryStats(stats)
                .flowVisualizations(flows)
                .build();
    }

    // Helper methods for validation
    private void validateLoadedQuantity(ShipmentCycle shipment) {
        Double totalLoaded = shipment.getTotalLoadedQuantity();
        if (totalLoaded > shipment.getTotalIncomingQuantity()) {
            throw new RuntimeException("Total loaded quantity (" + totalLoaded +
                    ") exceeds incoming quantity (" + shipment.getTotalIncomingQuantity() + ")");
        }
    }

    // Mapping methods
    private LighterLoading mapToLighterEntity(ShipmentDTO.LighterLoadingDTO dto) {
        return LighterLoading.builder()
                .lighterName(dto.getLighterName())
                .destination(dto.getDestination())
                .unloadingPoint(dto.getUnloadingPoint())
                .loadingDate(dto.getLoadingDate())
                .loadedQuantity(dto.getLoadedQuantity())
                .lighterCost(dto.getLighterCost())
                .status(dto.getStatus() != null ? dto.getStatus() : com.taskmanagement.enums.LoadUnloadStatus.PENDING)
                .lighterDocumentPath(dto.getLighterDocumentPath())
                .build();
    }

    private TruckUnloading mapToTruckEntity(ShipmentDTO.TruckUnloadingDTO dto) {
        return TruckUnloading.builder()
                .challan(dto.getChallan())
                .conveyanceName(dto.getConveyanceName())
                .numberOfTrucks(dto.getNumberOfTrucks())
                .dischargingLocation(dto.getDischargingLocation())
                .destination(dto.getDestination())
                .party(dto.getParty())
                .unloadingDate(dto.getUnloadingDate())
                .unloadedQuantity(dto.getUnloadedQuantity())
                .unloadingCost(dto.getUnloadingCost())
                .status(dto.getStatus() != null ? dto.getStatus() : com.taskmanagement.enums.LoadUnloadStatus.PENDING)
                .dependsOnLighterCompletion(dto.getDependsOnLighterCompletion() != null ? dto.getDependsOnLighterCompletion() : true)
                .build();
    }

    private ProductDetail mapToProductEntity(ShipmentDTO.ProductDetailDTO dto) {
        return ProductDetail.builder()
                .item(dto.getItem())
                .deliveryQuantity(dto.getDeliveryQuantity())
                .surveyQuantity(dto.getSurveyQuantity())
                .lighterCost(dto.getLighterCost())
                .unloadingCost(dto.getUnloadingCost())
                .truckTransportCost(dto.getTruckTransportCost())
                .build();
    }

    private ShipmentDTO mapToDTO(ShipmentCycle shipment) {
        return ShipmentDTO.builder()
                .id(shipment.getId())
                .consignee(shipment.getConsignee())
                .motherVesselName(shipment.getMotherVesselName())
                .arrivalDate(shipment.getArrivalDate())
                .totalIncomingQuantity(shipment.getTotalIncomingQuantity())
                .itemType(shipment.getItemType())
                .status(shipment.getStatus())
                .shipmentDocumentPath(shipment.getShipmentDocumentPath())
                .flowSummary(shipment.getFlowSummary())
                .instituteId(shipment.getInstitute().getId())
                .assignedToUserId(shipment.getAssignedTo() != null ? shipment.getAssignedTo().getId() : null)
                .assignedToName(shipment.getAssignedTo() != null ? shipment.getAssignedTo().getFullName() : null)
                .totalCost(shipment.calculateTotalCost())
                .totalLoadedQuantity(shipment.getTotalLoadedQuantity())
                .lighterCount(shipment.getLighterLoadings().size())
                .truckCount((int) shipment.getLighterLoadings().stream()
                        .mapToLong(l -> l.getTruckUnloadings().size()).sum())
                .lighterLoadings(shipment.getLighterLoadings().stream()
                        .map(this::mapToLighterDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private ShipmentDTO.LighterLoadingDTO mapToLighterDTO(LighterLoading lighter) {
        return ShipmentDTO.LighterLoadingDTO.builder()
                .id(lighter.getId())
                .lighterName(lighter.getLighterName())
                .destination(lighter.getDestination())
                .unloadingPoint(lighter.getUnloadingPoint())
                .loadingDate(lighter.getLoadingDate())
                .loadedQuantity(lighter.getLoadedQuantity())
                .lighterCost(lighter.getLighterCost())
                .status(lighter.getStatus())
                .lighterDocumentPath(lighter.getLighterDocumentPath())
                .totalCost(lighter.calculateTotalCost())
                .totalUnloadedQuantity(lighter.getTotalUnloadedQuantity())
                .remainingQuantity(lighter.getRemainingQuantity())
                .isBalanced(lighter.isQuantityBalanced())
                .truckUnloadings(lighter.getTruckUnloadings().stream()
                        .map(this::mapToTruckDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private ShipmentDTO.TruckUnloadingDTO mapToTruckDTO(TruckUnloading truck) {
        return ShipmentDTO.TruckUnloadingDTO.builder()
                .id(truck.getId())
                .challan(truck.getChallan())
                .conveyanceName(truck.getConveyanceName())
                .numberOfTrucks(truck.getNumberOfTrucks())
                .dischargingLocation(truck.getDischargingLocation())
                .destination(truck.getDestination())
                .party(truck.getParty())
                .unloadingDate(truck.getUnloadingDate())
                .unloadedQuantity(truck.getUnloadedQuantity())
                .unloadingCost(truck.getUnloadingCost())
                .status(truck.getStatus())
                .dependsOnLighterCompletion(truck.getDependsOnLighterCompletion())
                .sourceLighterName(truck.getSourceLighterName())
                .totalCost(truck.calculateTotalCost())
                .canProceed(truck.canProceed())
                .productDetails(truck.getProductDetails().stream()
                        .map(this::mapToProductDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private ShipmentDTO.ProductDetailDTO mapToProductDTO(ProductDetail product) {
        return ShipmentDTO.ProductDetailDTO.builder()
                .id(product.getId())
                .item(product.getItem())
                .deliveryQuantity(product.getDeliveryQuantity())
                .surveyQuantity(product.getSurveyQuantity())
                .lighterCost(product.getLighterCost())
                .unloadingCost(product.getUnloadingCost())
                .truckTransportCost(product.getTruckTransportCost())
                .totalCost(product.calculateTotalCost())
                .build();
    }
}
