package com.medcore.hms.inventory.service;

import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.inventory.domain.InventoryBatch;
import com.medcore.hms.inventory.domain.StockMovement;
import com.medcore.hms.inventory.dto.AdjustInventoryRequest;
import com.medcore.hms.inventory.dto.InventoryBatchResponse;
import com.medcore.hms.inventory.dto.ReceiveInventoryBatchRequest;
import com.medcore.hms.inventory.repository.InventoryBatchRepository;
import com.medcore.hms.inventory.repository.StockMovementRepository;
import com.medcore.hms.medicine.domain.Medicine;
import com.medcore.hms.medicine.repository.MedicineRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class InventoryBatchService {

    private final InventoryBatchRepository batchRepository;
    private final StockMovementRepository movementRepository;
    private final MedicineRepository medicineRepository;

    public InventoryBatchService(
            InventoryBatchRepository batchRepository,
            StockMovementRepository movementRepository,
            MedicineRepository medicineRepository) {

        this.batchRepository = batchRepository;
        this.movementRepository = movementRepository;
        this.medicineRepository = medicineRepository;
    }

    public InventoryBatchResponse receive(
            ReceiveInventoryBatchRequest input,
            Long actorUserId) {

        Medicine medicine =
                medicineRepository
                        .findByIdAndDeletedAtIsNull(
                                input.medicineId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Medicine",
                                        input.medicineId()));

        if (!medicine.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot receive stock for an inactive medicine");
        }

        String lotNumber = input.lotNumber().trim();

        if (batchRepository.existsByMedicineIdAndLotNumberIgnoreCase(
                input.medicineId(),
                lotNumber)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventory lot already exists for this medicine");
        }

        BigDecimal quantity = input.quantity();

        InventoryBatch batch =
                InventoryBatch.create();

        batch.setMedicineId(input.medicineId());
        batch.setLotNumber(lotNumber);
        batch.setExpiryDate(input.expiryDate());
        batch.setInitialQuantity(quantity);
        batch.setQuantityOnHand(quantity);
        batch.setUnitCost(input.unitCost());
        batch.setSupplierReference(
                trimToNull(input.supplierReference()));
        batch.setCreatedByUserId(actorUserId);
        batch.setUpdatedByUserId(actorUserId);

        InventoryBatch savedBatch =
                batchRepository.save(batch);

        StockMovement movement =
                StockMovement.receipt(
                        savedBatch.getId(),
                        quantity,
                        quantity,
                        actorUserId);

        movementRepository.save(movement);

        return toResponse(savedBatch);
    }

    @Transactional(readOnly = true)
    public List<InventoryBatchResponse> findAvailableByMedicineId(
            Long medicineId) {

        medicineRepository
                .findByIdAndDeletedAtIsNull(medicineId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine",
                                medicineId));

        return batchRepository
                .findAllByMedicineIdAndActiveTrueAndQuantityOnHandGreaterThanOrderByExpiryDateAsc(
                        medicineId,
                        BigDecimal.ZERO)
                .stream()
                .filter(batch ->
                        !batch.getExpiryDate()
                                .isBefore(LocalDate.now()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovement> findMovements(
            Long inventoryBatchId) {

        if (!batchRepository.existsById(inventoryBatchId)) {
            throw new ResourceNotFoundException(
                    "Inventory batch",
                    inventoryBatchId);
        }

        return movementRepository
                .findAllByInventoryBatchIdOrderByOccurredAtDesc(
                        inventoryBatchId);
    }

    public InventoryBatchResponse adjust(
            Long batchId,
            AdjustInventoryRequest input,
            Long actorUserId) {
        InventoryBatch batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory batch",
                                        batchId));
        if (!batch.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot adjust an inactive inventory batch");
        }
        BigDecimal delta = input.quantityDelta();
        if (delta.signum() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity delta must not be zero");
        }
        BigDecimal newQuantity =
                batch.getQuantityOnHand().add(delta);
        if (newQuantity.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Adjustment would make stock negative");
        }
        batch.setQuantityOnHand(newQuantity);
        batch.setUpdatedByUserId(actorUserId);
        InventoryBatch savedBatch =
                batchRepository.save(batch);
        StockMovement movement =
                StockMovement.adjustment(
                        savedBatch.getId(),
                        delta,
                        newQuantity,
                        input.reason().trim(),
                        actorUserId);
        movementRepository.save(movement);
        return toResponse(savedBatch);
    }

    private InventoryBatchResponse toResponse(
            InventoryBatch batch) {

        return new InventoryBatchResponse(
                batch.getId(),
                batch.getMedicineId(),
                batch.getLotNumber(),
                batch.getExpiryDate(),
                batch.getReceivedAt(),
                batch.getInitialQuantity(),
                batch.getQuantityOnHand(),
                batch.getUnitCost(),
                batch.getSupplierReference(),
                batch.isActive(),
                batch.getCreatedAt(),
                batch.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}