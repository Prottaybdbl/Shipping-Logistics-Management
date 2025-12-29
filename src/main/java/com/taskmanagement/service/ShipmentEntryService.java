package com.taskmanagement.service;

import com.taskmanagement.entity.ShipmentEntry;
import com.taskmanagement.repository.ShipmentEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShipmentEntryService {

    private final ShipmentEntryRepository shipmentEntryRepository;

    public ShipmentEntryService(ShipmentEntryRepository shipmentEntryRepository) {
        this.shipmentEntryRepository = shipmentEntryRepository;
    }

    public List<ShipmentEntry> findByBoardId(Long boardId) {
        return shipmentEntryRepository.findByBoardIdOrderByPositionAsc(boardId);
    }

    public Optional<ShipmentEntry> findById(Long id) {
        return shipmentEntryRepository.findById(id);
    }

    public ShipmentEntry save(ShipmentEntry entry) {
        return shipmentEntryRepository.save(entry);
    }

    public void deleteById(Long id) {
        shipmentEntryRepository.deleteById(id);
    }

    public boolean existsByChallanNo(String challanNo) {
        return shipmentEntryRepository.existsByChallanNo(challanNo);
    }

    public Long countByBoardId(Long boardId) {
        return shipmentEntryRepository.countByBoardId(boardId);
    }
}
