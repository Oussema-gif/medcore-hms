package com.medcore.hms.diagnosis.service;

import com.medcore.hms.diagnosis.domain.Diagnosis;
import com.medcore.hms.diagnosis.dto.CreateDiagnosisRequest;
import com.medcore.hms.diagnosis.dto.DiagnosisResponse;
import com.medcore.hms.diagnosis.repository.DiagnosisRepository;
import com.medcore.hms.exception.BadRequestException;
import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.medicalrecord.domain.MedicalRecord;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public DiagnosisService(
            DiagnosisRepository diagnosisRepository,
            MedicalRecordRepository medicalRecordRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public DiagnosisResponse create(
            CreateDiagnosisRequest request,
            Long actorUserId) {

        MedicalRecord medicalRecord = medicalRecordRepository
                .findById(request.medicalRecordId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medical record",
                                request.medicalRecordId()));

        validateCodePair(
                request.codeSystem(),
                request.diagnosisCode());

        if (request.primary()
                && request.diagnosisType()
                != com.medcore.hms.diagnosis.domain.DiagnosisType.PRIMARY) {
            throw new BadRequestException(
                    "A primary diagnosis must have diagnosisType PRIMARY");
        }

        if (!request.primary()
                && request.diagnosisType()
                == com.medcore.hms.diagnosis.domain.DiagnosisType.PRIMARY) {
            throw new BadRequestException(
                    "A PRIMARY diagnosis must be marked as primary");
        }

        if (request.primary()
                && diagnosisRepository
                .existsByMedicalRecordIdAndPrimaryTrue(
                        medicalRecord.getId())) {
            throw new BadRequestException(
                    "A primary diagnosis already exists for this medical record");
        }

        Diagnosis diagnosis = Diagnosis.create();
        diagnosis.setMedicalRecordId(medicalRecord.getId());
        diagnosis.setCodeSystem(normalize(request.codeSystem()));
        diagnosis.setDiagnosisCode(normalize(request.diagnosisCode()));
        diagnosis.setDescription(request.description().trim());
        diagnosis.setDiagnosisType(request.diagnosisType());
        diagnosis.setPrimary(request.primary());
        diagnosis.setCreatedByUserId(actorUserId);
        diagnosis.setUpdatedByUserId(actorUserId);

        return toResponse(diagnosisRepository.save(diagnosis));
    }

    @Transactional(readOnly = true)
    public DiagnosisResponse getById(Long diagnosisId) {
        return toResponse(getRequiredDiagnosis(diagnosisId));
    }

    @Transactional(readOnly = true)
    public Page<DiagnosisResponse> findByMedicalRecordId(
            Long medicalRecordId,
            Pageable pageable) {

        medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medical record",
                                medicalRecordId));

        return diagnosisRepository
                .findAllByMedicalRecordId(medicalRecordId, pageable)
                .map(this::toResponse);
    }

    private Diagnosis getRequiredDiagnosis(Long diagnosisId) {
        return diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Diagnosis",
                                diagnosisId));
    }

    private void validateCodePair(
            String codeSystem,
            String diagnosisCode) {

        boolean hasSystem = codeSystem != null
                && !codeSystem.isBlank();

        boolean hasCode = diagnosisCode != null
                && !diagnosisCode.isBlank();

        if (hasSystem != hasCode) {
            throw new BadRequestException(
                    "codeSystem and diagnosisCode must be provided together");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private DiagnosisResponse toResponse(Diagnosis diagnosis) {
        return new DiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getMedicalRecordId(),
                diagnosis.getCodeSystem(),
                diagnosis.getDiagnosisCode(),
                diagnosis.getDescription(),
                diagnosis.getDiagnosisType(),
                diagnosis.isPrimary(),
                diagnosis.getCreatedAt(),
                diagnosis.getUpdatedAt());
    }
}
