package com.medcore.hms.patient.service;

import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.patient.domain.Patient;
import com.medcore.hms.patient.domain.PatientSex;
import com.medcore.hms.patient.dto.CreatePatientRequest;
import com.medcore.hms.patient.dto.PatientResponse;
import com.medcore.hms.patient.dto.UpdatePatientRequest;
import com.medcore.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private PatientRepository patientRepository;
    private PatientService service;

    @BeforeEach
    void setUp() {
        patientRepository = mock(PatientRepository.class);
        service = new PatientService(
                patientRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private CreatePatientRequest fullCreateRequest() {
        return new CreatePatientRequest(
                "  MRN-001  ",
                "  Alice  ",
                "  Doe  ",
                LocalDate.of(1990, 5, 20),
                PatientSex.FEMALE,
                "alice@example.com",
                "+21611111111",
                "12 Rue de la Paix",
                null,
                "Monastir",
                "Monastir",
                "5000",
                "Tunisia",
                "Bob Doe",
                "+21622222222",
                "O+",
                "None known",
                null);
    }

    @Test
    void createsPatientWithTrimmedIdentityFields() {
        when(patientRepository.save(any(Patient.class)))
                .thenAnswer(invocation -> {
                    Patient saved = invocation.getArgument(0);
                    saved.setId(1L);
                    saved.setActive(true);
                    saved.setCreatedAt(NOW);
                    saved.setUpdatedAt(NOW);
                    return saved;
                });

        PatientResponse response = service.create(fullCreateRequest(), 10L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.medicalRecordNumber()).isEqualTo("MRN-001");
        assertThat(response.firstName()).isEqualTo("Alice");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.sex()).isEqualTo(PatientSex.FEMALE);
        assertThat(response.bloodGroup()).isEqualTo("O+");
        assertThat(response.active()).isTrue();

        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void getByIdReturnsResponseWhenPatientExistsAndNotDeleted() {
        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(5L);
        when(patient.getMedicalRecordNumber()).thenReturn("MRN-005");
        when(patient.getFirstName()).thenReturn("Carla");
        when(patient.getLastName()).thenReturn("Smith");
        when(patient.getSex()).thenReturn(PatientSex.FEMALE);
        when(patient.isActive()).thenReturn(true);
        when(patientRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(patient));

        PatientResponse response = service.getById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.medicalRecordNumber()).isEqualTo("MRN-005");
    }

    @Test
    void getByIdThrowsWhenPatientMissingOrDeleted() {
        when(patientRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllDelegatesToRepositoryAndExcludesDeletedRecords() {
        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(1L);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("lastName"));
        Page<Patient> page = new PageImpl<>(List.of(patient));
        when(patientRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(page);

        Page<PatientResponse> result = service.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    void updateAppliesOnlyProvidedFields() {
        Patient patient = mock(Patient.class);
        when(patientRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(patient));
        when(patientRepository.saveAndFlush(patient)).thenReturn(patient);
        when(patient.getId()).thenReturn(3L);

        UpdatePatientRequest request = new UpdatePatientRequest(
                null, "NewFirst", null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);

        service.update(3L, request, 20L);

        verify(patient).setFirstName("NewFirst");
        verify(patient, never()).setLastName(any());
        verify(patient, never()).setMedicalRecordNumber(any());
        verify(patient).setUpdatedByUserId(20L);
        verify(patientRepository).saveAndFlush(patient);
    }

    @Test
    void updateThrowsWhenPatientMissing() {
        when(patientRepository.findByIdAndDeletedAtIsNull(404L)).thenReturn(Optional.empty());

        UpdatePatientRequest request = new UpdatePatientRequest(
                null, "X", null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.update(404L, request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(patientRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteSoftDeletesPatient() {
        Patient patient = mock(Patient.class);
        when(patientRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(patient));

        service.delete(7L, 42L);

        verify(patient).softDelete(42L, NOW);
        verify(patient).setUpdatedByUserId(42L);
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void deleteThrowsWhenPatientMissing() {
        when(patientRepository.findByIdAndDeletedAtIsNull(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(patientRepository, never()).save(any());
    }
}