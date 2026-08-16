package com.medcore.hms.appointment.service;

import com.medcore.hms.appointment.domain.Appointment;
import com.medcore.hms.appointment.domain.AppointmentStatus;
import com.medcore.hms.appointment.dto.AppointmentResponse;
import com.medcore.hms.appointment.dto.CancelAppointmentRequest;
import com.medcore.hms.appointment.dto.CreateAppointmentRequest;
import com.medcore.hms.appointment.dto.UpdateAppointmentRequest;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.doctor.domain.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctoravailability.repository.DoctorAvailabilityRepository;
import com.medcore.hms.exception.BadRequestException;
import com.medcore.hms.exception.ConflictException;
import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.patient.domain.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final Clock clock;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository availabilityRepository,
            Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.clock = clock;
    }

    public AppointmentResponse create(
            CreateAppointmentRequest request,
            Long actorUserId) {

        Patient patient = getRequiredPatient(request.patientId());
        Doctor doctor = getRequiredDoctor(request.doctorId());

        validateDepartment(doctor, request.departmentId());
        validateTimeRange(
                request.scheduledStartAt(),
                request.scheduledEndAt());

        validateAvailability(
                request.doctorId(),
                request.scheduledStartAt(),
                request.scheduledEndAt());

        validateConflicts(
                null,
                request.patientId(),
                request.doctorId(),
                request.scheduledStartAt(),
                request.scheduledEndAt());

        Appointment appointment = Appointment.create();

        appointment.setPatientId(patient.getId());
        appointment.setDoctorId(doctor.getId());
        appointment.setDepartmentId(request.departmentId());
        appointment.setScheduledStartAt(request.scheduledStartAt());
        appointment.setScheduledEndAt(request.scheduledEndAt());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(request.reason().trim());
        appointment.setSchedulingNotes(request.schedulingNotes());
        appointment.setCreatedByUserId(actorUserId);
        appointment.setUpdatedByUserId(actorUserId);

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long appointmentId) {
        return toResponse(getRequiredAppointment(appointmentId));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findByDoctorId(
            Long doctorId,
            Pageable pageable) {

        getRequiredDoctor(doctorId);

        return appointmentRepository
                .findAllByDoctorId(doctorId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findByPatientId(
            Long patientId,
            Pageable pageable) {

        getRequiredPatient(patientId);

        return appointmentRepository
                .findAllByPatientId(patientId, pageable)
                .map(this::toResponse);
    }

    public AppointmentResponse update(
            Long appointmentId,
            UpdateAppointmentRequest request,
            Long actorUserId) {

        Appointment appointment = getRequiredAppointment(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException(
                    "Only scheduled appointments can be updated");
        }

        Instant start = request.scheduledStartAt() != null
                ? request.scheduledStartAt()
                : appointment.getScheduledStartAt();

        Instant end = request.scheduledEndAt() != null
                ? request.scheduledEndAt()
                : appointment.getScheduledEndAt();

        validateTimeRange(start, end);

        validateAvailability(
                appointment.getDoctorId(),
                start,
                end);

        validateConflicts(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                start,
                end);

        if (request.scheduledStartAt() != null) {
            appointment.setScheduledStartAt(start);
        }
        if (request.scheduledEndAt() != null) {
            appointment.setScheduledEndAt(end);
        }
        if (request.reason() != null) {
            appointment.setReason(request.reason().trim());
        }
        if (request.schedulingNotes() != null) {
            appointment.setSchedulingNotes(request.schedulingNotes());
        }

        appointment.setUpdatedByUserId(actorUserId);

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse checkIn(
            Long appointmentId,
            Long actorUserId) {

        Appointment appointment = getRequiredAppointment(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException(
                    "Only scheduled appointments can be checked in");
        }

        Instant now = clock.instant();
        var dayStart = now.atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        var dayEnd = dayStart.atZone(ZoneId.systemDefault())
                .plusDays(1)
                .toInstant();

        long currentQueueSize = appointmentRepository
                .countActiveQueueForDoctorOnDay(
                        appointment.getDoctorId(),
                        dayStart,
                        dayEnd);

        appointment.setCheckInAt(now);
        appointment.setQueuePosition((int) currentQueueSize + 1);
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setUpdatedByUserId(actorUserId);

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse startConsultation(
            Long appointmentId,
            Long actorUserId) {

        Appointment appointment = getRequiredAppointment(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new ConflictException(
                    "Only checked-in appointments can be started");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setUpdatedByUserId(actorUserId);

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse complete(
            Long appointmentId,
            Long actorUserId) {

        Appointment appointment = getRequiredAppointment(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new ConflictException(
                    "Only in-progress appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setQueuePosition(null);
        appointment.setUpdatedByUserId(actorUserId);

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse cancel(
            Long appointmentId,
            CancelAppointmentRequest request,
            Long actorUserId) {

        Appointment appointment = getRequiredAppointment(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new ConflictException(
                    "Only scheduled or checked-in appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(clock.instant());
        appointment.setCancellationReason(request.cancellationReason().trim());
        appointment.setQueuePosition(null);
        appointment.setUpdatedByUserId(actorUserId);

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse markNoShow(
            Long appointmentId,
            Long actorUserId) {

        Appointment appointment = getRequiredAppointment(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException(
                    "Only scheduled appointments can be marked as no-show");
        }

        if (!clock.instant().isAfter(appointment.getScheduledStartAt())) {
            throw new BadRequestException(
                    "Appointment cannot be marked as no-show before its scheduled start time");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setUpdatedByUserId(actorUserId);

        return toResponse(appointmentRepository.save(appointment));
    }

    private Patient getRequiredPatient(Long patientId) {
        return patientRepository.findByIdAndDeletedAtIsNull(patientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient", patientId));
    }

    private Doctor getRequiredDoctor(Long doctorId) {
        return doctorRepository.findByIdAndActiveTrue(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor", doctorId));
    }

    private Appointment getRequiredAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment",
                                appointmentId));
    }

    private void validateDepartment(
            Doctor doctor,
            Long departmentId) {

        if (!doctor.getDepartmentId().equals(departmentId)) {
            throw new BadRequestException(
                    "Department does not match the doctor's department");
        }
    }

    private void validateTimeRange(
            Instant start,
            Instant end) {

        if (start == null || end == null || !start.isBefore(end)) {
            throw new BadRequestException(
                    "scheduledStartAt must be before scheduledEndAt");
        }
    }

    private void validateAvailability(
            Long doctorId,
            Instant start,
            Instant end) {

        var startInZone = start.atZone(ZoneId.systemDefault());
        var endInZone = end.atZone(ZoneId.systemDefault());

        if (!startInZone.toLocalDate().equals(endInZone.toLocalDate())) {
            throw new BadRequestException(
                    "Appointment must start and end on the same date");
        }

        Short dayOfWeek = (short)
                startInZone.getDayOfWeek().getValue();

        boolean available = availabilityRepository
                .existsCoveringInterval(
                        doctorId,
                        dayOfWeek,
                        startInZone.toLocalDate(),
                        startInZone.toLocalTime(),
                        endInZone.toLocalTime());

        if (!available) {
            throw new BadRequestException(
                    "Appointment is outside the doctor's availability");
        }
    }

    private void validateConflicts(
            Long appointmentId,
            Long patientId,
            Long doctorId,
            Instant start,
            Instant end) {

        if (appointmentRepository.existsDoctorConflict(
                doctorId,
                appointmentId,
                start,
                end)) {
            throw new ConflictException(
                    "Doctor already has an overlapping appointment");
        }

        if (appointmentRepository.existsPatientConflict(
                patientId,
                appointmentId,
                start,
                end)) {
            throw new ConflictException(
                    "Patient already has an overlapping appointment");
        }
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDepartmentId(),
                appointment.getScheduledStartAt(),
                appointment.getScheduledEndAt(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getSchedulingNotes(),
                appointment.getCheckInAt(),
                appointment.getQueuePosition(),
                appointment.getCancelledAt(),
                appointment.getCancellationReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }
}