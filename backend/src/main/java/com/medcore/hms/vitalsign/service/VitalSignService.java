package com.medcore.hms.vitalsign.service;

import com.medcore.hms.vitalsign.domain.VitalSign;
import com.medcore.hms.vitalsign.dto.CreateVitalSignRequest;
import com.medcore.hms.vitalsign.dto.VitalSignResponse;
import com.medcore.hms.vitalsign.repository.VitalSignRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class VitalSignService {

    private final VitalSignRepository vitalSignRepository;

    public VitalSignService(VitalSignRepository vitalSignRepository) {
        this.vitalSignRepository = vitalSignRepository;
    }

    public VitalSignResponse create(CreateVitalSignRequest request) {
        validateBloodPressure(request);

        VitalSign vitalSign = VitalSign.create();
        vitalSign.setMedicalRecordId(request.medicalRecordId());
        vitalSign.setRecordedAt(request.recordedAt());
        vitalSign.setTemperatureCelsius(request.temperatureCelsius());
        vitalSign.setSystolicBp(request.systolicBp());
        vitalSign.setDiastolicBp(request.diastolicBp());
        vitalSign.setHeartRate(request.heartRate());
        vitalSign.setRespiratoryRate(request.respiratoryRate());
        vitalSign.setOxygenSaturation(request.oxygenSaturation());
        vitalSign.setWeightKg(request.weightKg());
        vitalSign.setHeightCm(request.heightCm());

        return toResponse(vitalSignRepository.save(vitalSign));
    }

    @Transactional(readOnly = true)
    public VitalSignResponse getById(Long id) {
        return vitalSignRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vital sign not found"));
    }

    @Transactional(readOnly = true)
    public Page<VitalSignResponse> findByMedicalRecordId(
            Long medicalRecordId,
            Pageable pageable) {

        return vitalSignRepository
                .findAllByMedicalRecordId(medicalRecordId, pageable)
                .map(this::toResponse);
    }

    private void validateBloodPressure(CreateVitalSignRequest request) {
        boolean systolicPresent = request.systolicBp() != null;
        boolean diastolicPresent = request.diastolicBp() != null;

        if (systolicPresent != diastolicPresent) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Systolic and diastolic blood pressure must be provided together");
        }

        if (systolicPresent
                && request.systolicBp() <= request.diastolicBp()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Systolic blood pressure must be greater than diastolic blood pressure");
        }
    }

    private VitalSignResponse toResponse(VitalSign vitalSign) {
        return new VitalSignResponse(
                vitalSign.getId(),
                vitalSign.getMedicalRecordId(),
                vitalSign.getRecordedAt(),
                vitalSign.getTemperatureCelsius(),
                vitalSign.getSystolicBp(),
                vitalSign.getDiastolicBp(),
                vitalSign.getHeartRate(),
                vitalSign.getRespiratoryRate(),
                vitalSign.getOxygenSaturation(),
                vitalSign.getWeightKg(),
                vitalSign.getHeightCm(),
                vitalSign.getCreatedAt(),
                vitalSign.getUpdatedAt());
    }
}
