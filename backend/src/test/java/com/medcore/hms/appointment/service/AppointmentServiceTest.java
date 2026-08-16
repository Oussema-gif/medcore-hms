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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceTest {

    // Wednesday, fixed "now" well before the scheduled window used in tests.
    private static final Instant NOW = Instant.parse("2026-01-05T08:00:00Z");

    private AppointmentRepository appointmentRepository;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private DoctorAvailabilityRepository availabilityRepository;
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        patientRepository = mock(PatientRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        availabilityRepository = mock(DoctorAvailabilityRepository.class);
        service = new AppointmentService(
                appointmentRepository,
                patientRepository,
                doctorRepository,
                availabilityRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private Patient patient(long id) {
        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(id);
        return patient;
    }

    private Doctor doctor(long id, long departmentId) {
        Doctor doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(id);
        when(doctor.getDepartmentId()).thenReturn(departmentId);
        return doctor;
    }

    private CreateAppointmentRequest createRequest(
            Instant start,
            Instant end) {
        return new CreateAppointmentRequest(
                1L, 2L, 3L, start, end, "Routine checkup", "Bring prior results");
    }

    @Test
    void createsAppointmentWhenAllValidationsPass() {
        Instant start = Instant.parse("2026-01-06T09:00:00Z");
        Instant end = Instant.parse("2026-01-06T09:30:00Z");

        Patient patientMock = patient(1L);
        Doctor doctorMock = doctor(2L, 3L);
        when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(patientMock));
        when(doctorRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(doctorMock));
        when(availabilityRepository.existsCoveringInterval(
                eq(2L), any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.existsDoctorConflict(eq(2L), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsPatientConflict(eq(1L), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> {
                    Appointment saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        AppointmentResponse response = service.create(createRequest(start, end), 50L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.reason()).isEqualTo("Routine checkup");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createThrowsWhenDepartmentDoesNotMatchDoctor() {
        Instant start = Instant.parse("2026-01-06T09:00:00Z");
        Instant end = Instant.parse("2026-01-06T09:30:00Z");

        Patient patientMock = patient(1L);
        Doctor doctorMock = doctor(2L, 999L);
        when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(patientMock));
        when(doctorRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(doctorMock));

        assertThatThrownBy(() -> service.create(createRequest(start, end), 50L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Department does not match the doctor's department");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenOutsideDoctorAvailability() {
        Instant start = Instant.parse("2026-01-06T09:00:00Z");
        Instant end = Instant.parse("2026-01-06T09:30:00Z");

        Patient patientMock = patient(1L);
        Doctor doctorMock = doctor(2L, 3L);
        when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(patientMock));
        when(doctorRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(doctorMock));
        when(availabilityRepository.existsCoveringInterval(
                eq(2L), any(), any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.create(createRequest(start, end), 50L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Appointment is outside the doctor's availability");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenDoctorHasOverlappingAppointment() {
        Instant start = Instant.parse("2026-01-06T09:00:00Z");
        Instant end = Instant.parse("2026-01-06T09:30:00Z");

        Patient patientMock = patient(1L);
        Doctor doctorMock = doctor(2L, 3L);
        when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(patientMock));
        when(doctorRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(doctorMock));
        when(availabilityRepository.existsCoveringInterval(
                eq(2L), any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.existsDoctorConflict(eq(2L), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest(start, end), 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Doctor already has an overlapping appointment");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenPatientMissing() {
        Instant start = Instant.parse("2026-01-06T09:00:00Z");
        Instant end = Instant.parse("2026-01-06T09:30:00Z");

        when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(createRequest(start, end), 50L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateThrowsConflictWhenAppointmentNotScheduled() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(null, null, "New reason", null);

        assertThatThrownBy(() -> service.update(1L, request, 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only scheduled appointments can be updated");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void updateAppliesOnlyProvidedFieldsWhenScheduled() {
        Appointment appointment = mock(Appointment.class);
        Instant existingStart = Instant.parse("2026-01-06T09:00:00Z");
        Instant existingEnd = Instant.parse("2026-01-06T09:30:00Z");
        when(appointment.getStatus()).thenReturn(AppointmentStatus.SCHEDULED);
        when(appointment.getScheduledStartAt()).thenReturn(existingStart);
        when(appointment.getScheduledEndAt()).thenReturn(existingEnd);
        when(appointment.getDoctorId()).thenReturn(2L);
        when(appointment.getPatientId()).thenReturn(1L);
        when(appointment.getId()).thenReturn(1L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(availabilityRepository.existsCoveringInterval(
                eq(2L), any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.existsDoctorConflict(eq(2L), eq(1L), any(), any())).thenReturn(false);
        when(appointmentRepository.existsPatientConflict(eq(1L), eq(1L), any(), any())).thenReturn(false);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        UpdateAppointmentRequest request = new UpdateAppointmentRequest(null, null, "Updated reason", null);

        service.update(1L, request, 50L);

        verify(appointment).setReason("Updated reason");
        verify(appointment, never()).setScheduledStartAt(any());
        verify(appointment).setUpdatedByUserId(50L);
    }

    @Test
    void checkInAssignsQueuePositionAndTransitionsStatus() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.SCHEDULED);
        when(appointment.getDoctorId()).thenReturn(2L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.countActiveQueueForDoctorOnDay(eq(2L), any(), any())).thenReturn(3L);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        service.checkIn(1L, 50L);

        verify(appointment).setCheckInAt(NOW);
        verify(appointment).setQueuePosition(4);
        verify(appointment).setStatus(AppointmentStatus.CHECKED_IN);
        verify(appointment).setUpdatedByUserId(50L);
    }

    @Test
    void checkInThrowsWhenAppointmentNotScheduled() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.checkIn(1L, 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only scheduled appointments can be checked in");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void startConsultationTransitionsFromCheckedInToInProgress() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.CHECKED_IN);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        service.startConsultation(1L, 50L);

        verify(appointment).setStatus(AppointmentStatus.IN_PROGRESS);
        verify(appointment).setUpdatedByUserId(50L);
    }

    @Test
    void startConsultationThrowsWhenNotCheckedIn() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.startConsultation(1L, 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only checked-in appointments can be started");
    }

    @Test
    void completeTransitionsFromInProgressAndClearsQueuePosition() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.IN_PROGRESS);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        service.complete(1L, 50L);

        verify(appointment).setStatus(AppointmentStatus.COMPLETED);
        verify(appointment).setQueuePosition(null);
        verify(appointment).setUpdatedByUserId(50L);
    }

    @Test
    void completeThrowsWhenNotInProgress() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.complete(1L, 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only in-progress appointments can be completed");
    }

    @Test
    void cancelAllowedFromScheduled() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        service.cancel(1L, new CancelAppointmentRequest("  Patient requested cancellation  "), 50L);

        verify(appointment).setStatus(AppointmentStatus.CANCELLED);
        verify(appointment).setCancelledAt(NOW);
        verify(appointment).setCancellationReason("Patient requested cancellation");
        verify(appointment).setQueuePosition(null);
    }

    @Test
    void cancelAllowedFromCheckedIn() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.CHECKED_IN);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        service.cancel(1L, new CancelAppointmentRequest("No longer needed"), 50L);

        verify(appointment).setStatus(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelThrowsWhenAppointmentAlreadyCompleted() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancel(1L, new CancelAppointmentRequest("Too late"), 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only scheduled or checked-in appointments can be cancelled");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void markNoShowSucceedsWhenScheduledTimeHasPassed() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.SCHEDULED);
        when(appointment.getScheduledStartAt()).thenReturn(Instant.parse("2026-01-05T07:00:00Z"));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        service.markNoShow(1L, 50L);

        verify(appointment).setStatus(AppointmentStatus.NO_SHOW);
        verify(appointment).setUpdatedByUserId(50L);
    }

    @Test
    void markNoShowThrowsWhenScheduledTimeInFuture() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.SCHEDULED);
        when(appointment.getScheduledStartAt()).thenReturn(Instant.parse("2026-01-05T09:00:00Z"));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.markNoShow(1L, 50L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Appointment cannot be marked as no-show before its scheduled start time");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void markNoShowThrowsWhenAppointmentNotScheduled() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.CHECKED_IN);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.markNoShow(1L, 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only scheduled appointments can be marked as no-show");
    }

    @Test
    void getByIdThrowsWhenAppointmentMissing() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByDoctorIdThrowsWhenDoctorMissing() {
        when(doctorRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByDoctorId(2L, org.springframework.data.domain.PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByPatientIdThrowsWhenPatientMissing() {
        when(patientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByPatientId(1L, org.springframework.data.domain.PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}