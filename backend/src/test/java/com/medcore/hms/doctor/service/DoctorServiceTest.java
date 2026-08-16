package com.medcore.hms.doctor.service;

import com.medcore.hms.doctor.domain.Doctor;
import com.medcore.hms.doctor.dto.CreateDoctorRequest;
import com.medcore.hms.doctor.dto.DoctorResponse;
import com.medcore.hms.doctor.dto.UpdateDoctorRequest;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.exception.ConflictException;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorServiceTest {

    private DoctorRepository doctorRepository;
    private DoctorService service;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        service = new DoctorService(doctorRepository);
    }

    private CreateDoctorRequest fullCreateRequest() {
        return new CreateDoctorRequest(
                1L,
                2L,
                "  REG-001  ",
                "  Alice  ",
                "  Doe  ",
                "Cardiology",
                "+21611111111",
                "alice@example.com",
                30);
    }

    @Test
    void createsDoctorWithTrimmedRegistrationNumberAndNames() {
        when(doctorRepository.existsByProfessionalRegistrationNumber("REG-001")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> {
                    Doctor saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        DoctorResponse response = service.create(fullCreateRequest(), 10L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.professionalRegistrationNumber()).isEqualTo("REG-001");
        assertThat(response.firstName()).isEqualTo("Alice");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.active()).isTrue();
        assertThat(response.consultationDurationMinutes()).isEqualTo(30);

        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void rejectsDuplicateRegistrationNumberWithConflict() {
        when(doctorRepository.existsByProfessionalRegistrationNumber("REG-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(fullCreateRequest(), 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Professional registration number already exists");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void getByIdReturnsResponseWhenActiveDoctorExists() {
        Doctor doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(5L);
        when(doctor.getFirstName()).thenReturn("Carla");
        when(doctor.getLastName()).thenReturn("Smith");
        when(doctor.isActive()).thenReturn(true);
        when(doctorRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(doctor));

        DoctorResponse response = service.getById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.firstName()).isEqualTo("Carla");
    }

    @Test
    void getByIdThrowsWhenDoctorMissingOrInactive() {
        when(doctorRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllDelegatesToRepositoryAndExcludesInactiveDoctors() {
        Doctor doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(1L);
        Pageable pageable = PageRequest.of(0, 20, Sort.by("lastName"));
        Page<Doctor> page = new PageImpl<>(List.of(doctor));
        when(doctorRepository.findAllByActiveTrue(pageable)).thenReturn(page);

        Page<DoctorResponse> result = service.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    void updateAppliesOnlyProvidedFields() {
        Doctor doctor = mock(Doctor.class);
        when(doctorRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctor.getId()).thenReturn(3L);

        UpdateDoctorRequest request = new UpdateDoctorRequest(
                null, null, null, "NewFirst", null, null, null, null, null);

        service.update(3L, request, 20L);

        verify(doctor).setFirstName("NewFirst");
        verify(doctor, never()).setLastName(any());
        verify(doctor, never()).setDepartmentId(any());
        verify(doctor, never()).setProfessionalRegistrationNumber(any());
        verify(doctor).setUpdatedByUserId(20L);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void updateThrowsWhenDoctorMissing() {
        when(doctorRepository.findByIdAndActiveTrue(404L)).thenReturn(Optional.empty());

        UpdateDoctorRequest request = new UpdateDoctorRequest(
                null, null, null, "X", null, null, null, null, null);

        assertThatThrownBy(() -> service.update(404L, request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void deleteDeactivatesDoctor() {
        Doctor doctor = mock(Doctor.class);
        when(doctorRepository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(doctor));

        service.delete(7L, 42L);

        verify(doctor).setActive(false);
        verify(doctor).setUpdatedByUserId(42L);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void deleteThrowsWhenDoctorMissing() {
        when(doctorRepository.findByIdAndActiveTrue(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorRepository, never()).save(any());
    }
}