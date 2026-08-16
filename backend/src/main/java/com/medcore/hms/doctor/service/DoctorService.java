package com.medcore.hms.doctor.service;

import com.medcore.hms.doctor.domain.Doctor;
import com.medcore.hms.doctor.dto.CreateDoctorRequest;
import com.medcore.hms.doctor.dto.DoctorResponse;
import com.medcore.hms.doctor.dto.UpdateDoctorRequest;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.exception.ConflictException;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public DoctorResponse create(
            CreateDoctorRequest request,
            Long actorUserId) {

        String registrationNumber =
                request.professionalRegistrationNumber().trim();

        if (doctorRepository
                .existsByProfessionalRegistrationNumber(registrationNumber)) {
            throw new ConflictException(
                    "Professional registration number already exists");
        }

        Doctor doctor = Doctor.create();

        doctor.setUserAccountId(request.userAccountId());
        doctor.setDepartmentId(request.departmentId());
        doctor.setProfessionalRegistrationNumber(registrationNumber);
        doctor.setFirstName(request.firstName().trim());
        doctor.setLastName(request.lastName().trim());
        doctor.setSpecialty(request.specialty());
        doctor.setPhoneNumber(request.phoneNumber());
        doctor.setEmail(request.email());
        doctor.setConsultationDurationMinutes(
                request.consultationDurationMinutes());
        doctor.setActive(true);
        doctor.setCreatedByUserId(actorUserId);
        doctor.setUpdatedByUserId(actorUserId);

        return toResponse(doctorRepository.save(doctor));
    }

    @Transactional(readOnly = true)
    public DoctorResponse getById(Long doctorId) {
        return toResponse(getRequired(doctorId));
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> findAll(Pageable pageable) {
        return doctorRepository
                .findAllByActiveTrue(pageable)
                .map(this::toResponse);
    }

    public DoctorResponse update(
            Long doctorId,
            UpdateDoctorRequest request,
            Long actorUserId) {

        Doctor doctor = getRequired(doctorId);

        if (request.userAccountId() != null) {
            doctor.setUserAccountId(request.userAccountId());
        }
        if (request.departmentId() != null) {
            doctor.setDepartmentId(request.departmentId());
        }
        if (request.professionalRegistrationNumber() != null) {
            doctor.setProfessionalRegistrationNumber(
                    request.professionalRegistrationNumber().trim());
        }
        if (request.firstName() != null) {
            doctor.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            doctor.setLastName(request.lastName().trim());
        }
        if (request.specialty() != null) {
            doctor.setSpecialty(request.specialty());
        }
        if (request.phoneNumber() != null) {
            doctor.setPhoneNumber(request.phoneNumber());
        }
        if (request.email() != null) {
            doctor.setEmail(request.email());
        }
        if (request.consultationDurationMinutes() != null) {
            doctor.setConsultationDurationMinutes(
                    request.consultationDurationMinutes());
        }

        doctor.setUpdatedByUserId(actorUserId);

        return toResponse(doctorRepository.save(doctor));
    }

    public void delete(Long doctorId, Long actorUserId) {
        Doctor doctor = getRequired(doctorId);
        doctor.setActive(false);
        doctor.setUpdatedByUserId(actorUserId);
        doctorRepository.save(doctor);
    }

    private Doctor getRequired(Long doctorId) {
        return doctorRepository.findByIdAndActiveTrue(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
    }

    private DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getUserAccountId(),
                doctor.getDepartmentId(),
                doctor.getProfessionalRegistrationNumber(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialty(),
                doctor.getPhoneNumber(),
                doctor.getEmail(),
                doctor.getConsultationDurationMinutes(),
                doctor.isActive(),
                doctor.getCreatedAt(),
                doctor.getUpdatedAt());
    }
}