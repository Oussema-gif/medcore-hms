package com.medcore.hms.inventory.repository;

import com.medcore.hms.inventory.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement>
    findAllByInventoryBatchIdOrderByOccurredAtDesc(
            Long inventoryBatchId);
}
