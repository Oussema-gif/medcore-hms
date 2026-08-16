package com.medcore.hms.inventory.repository;

import com.medcore.hms.inventory.domain.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryBatchRepository
        extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch>
    findAllByMedicineIdAndActiveTrueAndQuantityOnHandGreaterThanOrderByExpiryDateAsc(
            Long medicineId,
            java.math.BigDecimal quantity);

    boolean existsByMedicineIdAndLotNumberIgnoreCase(
            Long medicineId,
            String lotNumber);
}
