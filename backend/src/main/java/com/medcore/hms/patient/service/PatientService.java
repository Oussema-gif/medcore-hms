package com.medcore.hms.patient.service;

import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.patient.domain.Patient;
import com.medcore.hms.patient.dto.CreatePatientRequest;
import com.medcore.hms.patient.dto.PatientResponse;
import com.medcore.hms.patient.dto.UpdatePatientRequest;
import com.medcore.hms.patient.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final Clock clock;

    public PatientService(
            PatientRepository patientRepository,
            Clock clock) {
        this.patientRepository = patientRepository;
        this.clock = clock;
    }

    public PatientResponse create(
            CreatePatientRequest request,
            Long actorUserId) {
        Patient patient = Patient.create();

        patient.setUserAccountId(request.userAccountId());
        patient.setMedicalRecordNumber(request.medicalRecordNumber().trim());
        patient.setFirstName(request.firstName().trim());
        patient.setLastName(request.lastName().trim());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setSex(request.sex());
        patient.setEmail(request.email());
        patient.setPhoneNumber(request.phoneNumber());
        patient.setAddressLine1(request.addressLine1());
        patient.setAddressLine2(request.addressLine2());
        patient.setCity(request.city());
        patient.setState(request.state());
        patient.setPostalCode(request.postalCode());
        patient.setCountry(request.country());
        patient.setEmergencyContactName(request.emergencyContactName());
        patient.setEmergencyContactPhone(request.emergencyContactPhone());
        patient.setBloodGroup(request.bloodGroup());
        patient.setAllergies(request.allergies());
        patient.setCreatedByUserId(actorUserId);
        patient.setUpdatedByUserId(actorUserId);

        return toResponse(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long patientId) {
        return toResponse(getRequired(patientId));
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(Pageable pageable) {
        return patientRepository
                .findAllByDeletedAtIsNull(pageable)
                .map(this::toResponse);
    }

    public PatientResponse update(
            Long patientId,
            UpdatePatientRequest request,
            Long actorUserId) {
        Patient patient = getRequired(patientId);

        if (request.userAccountId() != null) {
            patient.setUserAccountId(request.userAccountId());
        }
        if (request.medicalRecordNumber() != null) {
            patient.setMedicalRecordNumber(request.medicalRecordNumber().trim());
        }
        if (request.firstName() != null) {
            patient.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            patient.setLastName(request.lastName().trim());
        }
        if (request.dateOfBirth() != null) {
            patient.setDateOfBirth(request.dateOfBirth());
        }
        if (request.sex() != null) {
            patient.setSex(request.sex());
        }
        if (request.email() != null) {
            patient.setEmail(request.email());
        }
        if (request.phoneNumber() != null) {
            patient.setPhoneNumber(request.phoneNumber());
        }
        if (request.addressLine1() != null) {
            patient.setAddressLine1(request.addressLine1());
        }
        if (request.addressLine2() != null) {
            patient.setAddressLine2(request.addressLine2());
        }
        if (request.city() != null) {
            patient.setCity(request.city());
        }
        if (request.state() != null) {
            patient.setState(request.state());
        }
        if (request.postalCode() != null) {
            patient.setPostalCode(request.postalCode());
        }
        if (request.country() != null) {
            patient.setCountry(request.country());
        }
        if (request.emergencyContactName() != null) {
            patient.setEmergencyContactName(request.emergencyContactName());
        }
        if (request.emergencyContactPhone() != null) {
            patient.setEmergencyContactPhone(request.emergencyContactPhone());
        }
        if (request.bloodGroup() != null) {
            patient.setBloodGroup(request.bloodGroup());
        }
        if (request.allergies() != null) {
            patient.setAllergies(request.allergies());
        }

        patient.setUpdatedByUserId(actorUserId);

        Patient savedPatient = patientRepository.saveAndFlush(patient);
        return toResponse(savedPatient);
    }

    public void delete(Long patientId, Long actorUserId) {
        Patient patient = getRequired(patientId);
        patient.softDelete(actorUserId, Instant.now(clock));
        patient.setUpdatedByUserId(actorUserId);
        patientRepository.save(patient);
    }

    private Patient getRequired(Long patientId) {
        return patientRepository.findByIdAndDeletedAtIsNull(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getUserAccountId(),
                patient.getMedicalRecordNumber(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getSex(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getAddressLine1(),
                patient.getAddressLine2(),
                patient.getCity(),
                patient.getState(),
                patient.getPostalCode(),
                patient.getCountry(),
                patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(),
                patient.getBloodGroup(),
                patient.getAllergies(),
                patient.isActive(),
                patient.getCreatedAt(),
                patient.getUpdatedAt());
    }
}