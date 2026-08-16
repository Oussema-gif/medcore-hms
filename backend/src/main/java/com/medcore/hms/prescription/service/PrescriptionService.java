package com.medcore.hms.prescription.service;

import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.inventory.domain.InventoryBatch;
import com.medcore.hms.inventory.domain.StockMovement;
import com.medcore.hms.inventory.repository.InventoryBatchRepository;
import com.medcore.hms.inventory.repository.StockMovementRepository;
import com.medcore.hms.medicine.domain.Medicine;
import com.medcore.hms.medicine.repository.MedicineRepository;
import com.medcore.hms.prescription.domain.Prescription;
import com.medcore.hms.prescription.domain.PrescriptionItem;
import com.medcore.hms.prescription.domain.PrescriptionStatus;
import com.medcore.hms.prescription.dto.CancelPrescriptionRequest;
import com.medcore.hms.prescription.dto.CreatePrescriptionRequest;
import com.medcore.hms.prescription.dto.PrescriptionResponse;
import com.medcore.hms.prescription.repository.PrescriptionItemRepository;
import com.medcore.hms.prescription.repository.PrescriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository itemRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryBatchRepository batchRepository;
    private final StockMovementRepository movementRepository;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            PrescriptionItemRepository itemRepository,
            MedicineRepository medicineRepository,
            InventoryBatchRepository batchRepository,
            StockMovementRepository movementRepository) {

        this.prescriptionRepository = prescriptionRepository;
        this.itemRepository = itemRepository;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.movementRepository = movementRepository;
    }

    public PrescriptionResponse create(
            CreatePrescriptionRequest input,
            Long actorUserId) {

        for (CreatePrescriptionRequest.Item inputItem
                : input.items()) {

            Medicine medicine =
                    medicineRepository
                            .findByIdAndDeletedAtIsNull(
                                    inputItem.medicineId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Medicine",
                                            inputItem.medicineId()));

            if (!medicine.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot prescribe an inactive medicine");
            }
        }

        Prescription prescription =
                Prescription.create();

        prescription.setPatientId(input.patientId());
        prescription.setDoctorId(input.doctorId());
        prescription.setAppointmentId(input.appointmentId());
        prescription.setMedicalRecordId(input.medicalRecordId());
        prescription.setExpiresAt(input.expiresAt());
        prescription.setNotes(trimToNull(input.notes()));
        prescription.setCreatedByUserId(actorUserId);
        prescription.setUpdatedByUserId(actorUserId);

        Prescription savedPrescription =
                prescriptionRepository.save(prescription);

        List<PrescriptionItem> items =
                input.items()
                        .stream()
                        .map(inputItem -> {
                            PrescriptionItem item =
                                    PrescriptionItem.create();

                            item.setPrescriptionId(
                                    savedPrescription.getId());
                            item.setMedicineId(
                                    inputItem.medicineId());
                            item.setDosage(
                                    inputItem.dosage().trim());
                            item.setRoute(
                                    inputItem.route().trim());
                            item.setFrequency(
                                    inputItem.frequency().trim());
                            item.setDurationDays(
                                    inputItem.durationDays());
                            item.setQuantityPrescribed(
                                    inputItem.quantityPrescribed());
                            item.setInstructions(
                                    trimToNull(inputItem.instructions()));

                            return item;
                        })
                        .toList();

        itemRepository.saveAll(items);

        return toResponse(
                savedPrescription,
                items);
    }

    public PrescriptionResponse cancel(
            Long prescriptionId,
            CancelPrescriptionRequest input,
            Long actorUserId) {
        Prescription prescription =
                prescriptionRepository.findById(prescriptionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Prescription",
                                        prescriptionId));
        if (prescription.getStatus()
                == PrescriptionStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Prescription is already cancelled");
        }
        if (prescription.getStatus()
                == PrescriptionStatus.DISPENSED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A dispensed prescription cannot be cancelled");
        }
        String reason =
                input.cancellationReason().trim();
        if (reason.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cancellation reason must not be blank");
        }
        Instant now = Instant.now();
        prescription.setStatus(
                PrescriptionStatus.CANCELLED);
        prescription.setCancelledAt(now);
        prescription.setCancellationReason(reason);
        prescription.setUpdatedAt(now);
        prescription.setUpdatedByUserId(actorUserId);
        Prescription savedPrescription =
                prescriptionRepository.save(prescription);
        return toResponse(
                savedPrescription,
                itemRepository
                        .findAllByPrescriptionIdOrderByIdAsc(
                                prescriptionId));
    }

    public PrescriptionResponse dispense(
            Long prescriptionId,
            Long actorUserId) {
        Prescription prescription =
                prescriptionRepository.findById(prescriptionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Prescription",
                                        prescriptionId));
        if (prescription.getStatus()
                != PrescriptionStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only an active prescription can be dispensed");
        }
        if (prescription.getExpiresAt() != null
                && prescription.getExpiresAt()
                        .isBefore(Instant.now())) {
            prescription.setStatus(
                    PrescriptionStatus.EXPIRED);
            prescription.setUpdatedByUserId(actorUserId);
            prescriptionRepository.save(prescription);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Prescription has expired");
        }
        List<PrescriptionItem> items =
                itemRepository
                        .findAllByPrescriptionIdOrderByIdAsc(
                                prescriptionId);
        if (items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Prescription has no items");
        }
        for (PrescriptionItem item : items) {
            dispenseItem(
                    prescriptionId,
                    item,
                    actorUserId);
        }
        prescription.setStatus(
                PrescriptionStatus.DISPENSED);
        prescription.setUpdatedByUserId(actorUserId);
        Prescription savedPrescription =
                prescriptionRepository.save(prescription);
        return toResponse(
                savedPrescription,
                items);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getById(
            Long prescriptionId) {

        Prescription prescription =
                prescriptionRepository.findById(prescriptionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Prescription",
                                        prescriptionId));

        return toResponse(
                prescription,
                itemRepository
                        .findAllByPrescriptionIdOrderByIdAsc(
                                prescriptionId));
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> findByPatientId(
            Long patientId,
            Pageable pageable) {

        return prescriptionRepository
                .findAllByPatientId(patientId, pageable)
                .map(prescription ->
                        toResponse(
                                prescription,
                                itemRepository
                                        .findAllByPrescriptionIdOrderByIdAsc(
                                                prescription.getId())));
    }

    private void dispenseItem(
            Long prescriptionId,
            PrescriptionItem item,
            Long actorUserId) {
        BigDecimal remaining =
                item.getQuantityPrescribed();
        List<InventoryBatch> batches =
                batchRepository
                        .findAllByMedicineIdAndActiveTrueAndQuantityOnHandGreaterThanOrderByExpiryDateAsc(
                                item.getMedicineId(),
                                BigDecimal.ZERO)
                        .stream()
                        .filter(batch ->
                                !batch.getExpiryDate()
                                        .isBefore(
                                                java.time.LocalDate.now()))
                        .toList();
        BigDecimal available =
                batches.stream()
                        .map(InventoryBatch::getQuantityOnHand)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (available.compareTo(remaining) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient stock for medicine "
                            + item.getMedicineId());
        }
        for (InventoryBatch batch : batches) {
            if (remaining.signum() <= 0) {
                break;
            }
            BigDecimal deduction =
                    batch.getQuantityOnHand()
                            .min(remaining);
            BigDecimal quantityAfter =
                    batch.getQuantityOnHand()
                            .subtract(deduction);
            batch.setQuantityOnHand(quantityAfter);
            batch.setUpdatedByUserId(actorUserId);
            batchRepository.save(batch);
            StockMovement movement =
                    StockMovement.dispense(
                            batch.getId(),
                            deduction.negate(),
                            quantityAfter,
                            prescriptionId,
                            item.getId(),
                            actorUserId);
            movementRepository.save(movement);
            remaining = remaining.subtract(deduction);
        }
    }

    private PrescriptionResponse toResponse(
            Prescription prescription,
            List<PrescriptionItem> items) {

        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPatientId(),
                prescription.getDoctorId(),
                prescription.getAppointmentId(),
                prescription.getMedicalRecordId(),
                prescription.getStatus(),
                prescription.getPrescribedAt(),
                prescription.getExpiresAt(),
                prescription.getNotes(),
                prescription.getCancelledAt(),
                prescription.getCancellationReason(),
                items.stream()
                        .map(item ->
                                new PrescriptionResponse.Item(
                                        item.getId(),
                                        item.getMedicineId(),
                                        item.getDosage(),
                                        item.getRoute(),
                                        item.getFrequency(),
                                        item.getDurationDays(),
                                        item.getQuantityPrescribed(),
                                        item.getInstructions(),
                                        item.getCreatedAt(),
                                        item.getUpdatedAt()))
                        .toList());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}