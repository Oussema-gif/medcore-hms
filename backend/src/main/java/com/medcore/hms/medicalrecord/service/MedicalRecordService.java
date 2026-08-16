package com.medcore.hms.medicalrecord.service;

import com.medcore.hms.appointment.domain.Appointment;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.doctor.domain.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.exception.BadRequestException;
import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.medicalrecord.domain.MedicalRecord;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequest;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponse;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequest;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.patient.domain.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public MedicalRecordResponse create(
            CreateMedicalRecordRequest request,
            Long actorUserId) {

        Patient patient = patientRepository
                .findByIdAndDeletedAtIsNull(request.patientId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient",
                                request.patientId()));

        Doctor doctor = doctorRepository
                .findByIdAndActiveTrue(request.doctorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor",
                                request.doctorId()));

        Appointment appointment = null;

        if (request.appointmentId() != null) {
            appointment = appointmentRepository
                    .findById(request.appointmentId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Appointment",
                                    request.appointmentId()));

            if (!appointment.getPatientId().equals(patient.getId())
                    || !appointment.getDoctorId().equals(doctor.getId())) {
                throw new BadRequestException(
                        "Appointment does not match the patient and doctor");
            }
        }

        MedicalRecord record = MedicalRecord.create();
        record.setPatientId(patient.getId());
        record.setDoctorId(doctor.getId());
        record.setAppointmentId(
                appointment == null ? null : appointment.getId());
        record.setEncounterAt(request.encounterAt());
        record.setSymptoms(request.symptoms());
        record.setClinicalNotes(request.clinicalNotes().trim());
        record.setCreatedByUserId(actorUserId);
        record.setUpdatedByUserId(actorUserId);

        return toResponse(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getById(Long medicalRecordId) {
        return toResponse(getRequiredRecord(medicalRecordId));
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> findByPatientId(
            Long patientId,
            Pageable pageable) {

        patientRepository.findByIdAndDeletedAtIsNull(patientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient", patientId));

        return medicalRecordRepository
                .findAllByPatientId(patientId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> findByDoctorId(
            Long doctorId,
            Pageable pageable) {

        doctorRepository.findByIdAndActiveTrue(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor", doctorId));

        return medicalRecordRepository
                .findAllByDoctorId(doctorId, pageable)
                .map(this::toResponse);
    }

    public MedicalRecordResponse update(
            Long medicalRecordId,
            UpdateMedicalRecordRequest request,
            Long actorUserId) {
        MedicalRecord record = getRequiredRecord(medicalRecordId);
        if (request.encounterAt() != null) {
            record.setEncounterAt(request.encounterAt());
        }
        if (request.symptoms() != null) {
            record.setSymptoms(request.symptoms());
        }
        if (request.clinicalNotes() != null) {
            String notes = request.clinicalNotes().trim();
            if (notes.isBlank()) {
                throw new BadRequestException(
                        "Clinical notes must not be blank");
            }
            record.setClinicalNotes(notes);
        }
        record.setUpdatedByUserId(actorUserId);
        return toResponse(
                medicalRecordRepository.saveAndFlush(record));
    }

    public MedicalRecordResponse release(
            Long medicalRecordId,
            Long actorUserId) {
        MedicalRecord record = getRequiredRecord(medicalRecordId);
        if (!record.isPatientVisible()) {
            record.setPatientVisible(true);
            record.setReleasedAt(java.time.Instant.now());
            record.setUpdatedByUserId(actorUserId);
        }
        return toResponse(
                medicalRecordRepository.saveAndFlush(record));
    }

    private MedicalRecord getRequiredRecord(Long medicalRecordId) {
        return medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medical record",
                                medicalRecordId));
    }

    private MedicalRecordResponse toResponse(MedicalRecord record) {
        return new MedicalRecordResponse(
                record.getId(),
                record.getPatientId(),
                record.getDoctorId(),
                record.getAppointmentId(),
                record.getEncounterAt(),
                record.getSymptoms(),
                record.getClinicalNotes(),
                record.isPatientVisible(),
                record.getReleasedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt());
    }
}