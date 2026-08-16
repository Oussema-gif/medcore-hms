package com.medcore.hms.medicine.service;

import com.medcore.hms.medicine.domain.Medicine;
import com.medcore.hms.medicine.dto.CreateMedicineRequest;
import com.medcore.hms.medicine.dto.MedicineResponse;
import com.medcore.hms.medicine.dto.UpdateMedicineRequest;
import com.medcore.hms.medicine.repository.MedicineRepository;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

@Service
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final Clock clock;

    public MedicineService(
            MedicineRepository medicineRepository,
            Clock clock) {
        this.medicineRepository = medicineRepository;
        this.clock = clock;
    }

    public MedicineResponse create(
            CreateMedicineRequest request,
            Long actorUserId) {

        String code = request.medicineCode().trim();

        if (medicineRepository.existsByMedicineCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Medicine code already exists");
        }

        Medicine medicine = Medicine.create();
        medicine.setMedicineCode(code);
        medicine.setName(request.name().trim());
        medicine.setGenericName(trimToNull(request.genericName()));
        medicine.setDosageForm(request.dosageForm().trim());
        medicine.setStrength(trimToNull(request.strength()));
        medicine.setUnit(request.unit().trim());
        medicine.setReorderLevel(
                request.reorderLevel() == null
                        ? BigDecimal.ZERO
                        : request.reorderLevel());
        medicine.setCreatedByUserId(actorUserId);
        medicine.setUpdatedByUserId(actorUserId);

        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional(readOnly = true)
    public MedicineResponse getById(Long medicineId) {
        return toResponse(getRequired(medicineId));
    }

    @Transactional(readOnly = true)
    public Page<MedicineResponse> findAll(Pageable pageable) {
        return medicineRepository
                .findAllByDeletedAtIsNull(pageable)
                .map(this::toResponse);
    }

    public MedicineResponse update(
            Long medicineId,
            UpdateMedicineRequest request,
            Long actorUserId) {

        Medicine medicine = getRequired(medicineId);

        if (request.medicineCode() != null) {
            String code = request.medicineCode().trim();
            if (medicineRepository
                    .existsByMedicineCodeIgnoreCaseAndIdNot(
                            code,
                            medicineId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Medicine code already exists");
            }
            medicine.setMedicineCode(code);
        }
        if (request.name() != null) {
            medicine.setName(request.name().trim());
        }
        if (request.genericName() != null) {
            medicine.setGenericName(trimToNull(request.genericName()));
        }
        if (request.dosageForm() != null) {
            medicine.setDosageForm(request.dosageForm().trim());
        }
        if (request.strength() != null) {
            medicine.setStrength(trimToNull(request.strength()));
        }
        if (request.unit() != null) {
            medicine.setUnit(request.unit().trim());
        }
        if (request.reorderLevel() != null) {
            medicine.setReorderLevel(request.reorderLevel());
        }

        medicine.setUpdatedByUserId(actorUserId);

        return toResponse(
                medicineRepository.saveAndFlush(medicine));
    }

    public void delete(
            Long medicineId,
            Long actorUserId) {

        Medicine medicine = getRequired(medicineId);
        medicine.softDelete(actorUserId, Instant.now(clock));
        medicine.setUpdatedByUserId(actorUserId);
        medicineRepository.save(medicine);
    }

    private Medicine getRequired(Long medicineId) {
        return medicineRepository
                .findByIdAndDeletedAtIsNull(medicineId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medicine",
                                medicineId));
    }

    private MedicineResponse toResponse(Medicine medicine) {
        return new MedicineResponse(
                medicine.getId(),
                medicine.getMedicineCode(),
                medicine.getName(),
                medicine.getGenericName(),
                medicine.getDosageForm(),
                medicine.getStrength(),
                medicine.getUnit(),
                medicine.getReorderLevel(),
                medicine.isActive(),
                medicine.getCreatedAt(),
                medicine.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
