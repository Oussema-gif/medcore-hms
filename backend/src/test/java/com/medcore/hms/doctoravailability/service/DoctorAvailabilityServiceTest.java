package com.medcore.hms.doctoravailability.service;

import com.medcore.hms.doctor.domain.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctoravailability.domain.DoctorAvailability;
import com.medcore.hms.doctoravailability.dto.CreateDoctorAvailabilityRequest;
import com.medcore.hms.doctoravailability.dto.DoctorAvailabilityResponse;
import com.medcore.hms.doctoravailability.dto.UpdateDoctorAvailabilityRequest;
import com.medcore.hms.doctoravailability.repository.DoctorAvailabilityRepository;
import com.medcore.hms.exception.BadRequestException;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorAvailabilityServiceTest {

    private DoctorAvailabilityRepository availabilityRepository;
    private DoctorRepository doctorRepository;
    private DoctorAvailabilityService service;

    @BeforeEach
    void setUp() {
        availabilityRepository = mock(DoctorAvailabilityRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        service = new DoctorAvailabilityService(availabilityRepository, doctorRepository);
    }

    private CreateDoctorAvailabilityRequest validCreateRequest() {
        return new CreateDoctorAvailabilityRequest(
                1L,
                (short) 2,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31));
    }

    @Test
    void createsAvailabilityWhenDoctorExistsAndRangesValid() {
        Doctor doctor = mock(Doctor.class);
        when(doctorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.save(any(DoctorAvailability.class)))
                .thenAnswer(invocation -> {
                    DoctorAvailability saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        DoctorAvailabilityResponse response = service.create(validCreateRequest(), 10L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.doctorId()).isEqualTo(1L);
        assertThat(response.dayOfWeek()).isEqualTo((short) 2);
        assertThat(response.active()).isTrue();

        verify(availabilityRepository).save(any(DoctorAvailability.class));
    }

    @Test
    void createThrowsWhenDoctorMissing() {
        when(doctorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validCreateRequest(), 10L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenStartTimeNotBeforeEndTime() {
        Doctor doctor = mock(Doctor.class);
        when(doctorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(doctor));

        CreateDoctorAvailabilityRequest request = new CreateDoctorAvailabilityRequest(
                1L,
                (short) 2,
                LocalTime.of(17, 0),
                LocalTime.of(9, 0),
                LocalDate.of(2026, 1, 1),
                null);

        assertThatThrownBy(() -> service.create(request, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("startTime must be before endTime");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenEffectiveUntilBeforeEffectiveFrom() {
        Doctor doctor = mock(Doctor.class);
        when(doctorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(doctor));

        CreateDoctorAvailabilityRequest request = new CreateDoctorAvailabilityRequest(
                1L,
                (short) 2,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> service.create(request, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("effectiveUntil must be on or after effectiveFrom");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void getByIdReturnsResponseWhenActiveAvailabilityExists() {
        DoctorAvailability availability = mock(DoctorAvailability.class);
        when(availability.getId()).thenReturn(5L);
        when(availability.isActive()).thenReturn(true);
        when(availabilityRepository.findById(5L)).thenReturn(Optional.of(availability));

        DoctorAvailabilityResponse response = service.getById(5L);

        assertThat(response.id()).isEqualTo(5L);
    }

    @Test
    void getByIdThrowsWhenAvailabilityMissing() {
        when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIdThrowsWhenAvailabilityInactive() {
        DoctorAvailability availability = mock(DoctorAvailability.class);
        when(availability.isActive()).thenReturn(false);
        when(availabilityRepository.findById(5L)).thenReturn(Optional.of(availability));

        assertThatThrownBy(() -> service.getById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByDoctorIdValidatesDoctorThenDelegatesToRepository() {
        Doctor doctor = mock(Doctor.class);
        DoctorAvailability availability = mock(DoctorAvailability.class);
        when(availability.getId()).thenReturn(1L);
        when(doctorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(doctor));
        Pageable pageable = PageRequest.of(0, 20);
        when(availabilityRepository.findAllByDoctorIdAndActiveTrue(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(availability)));

        Page<DoctorAvailabilityResponse> result = service.findByDoctorId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByDoctorIdThrowsWhenDoctorMissing() {
        when(doctorRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> service.findByDoctorId(1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(availabilityRepository, never()).findAllByDoctorIdAndActiveTrue(any(), any());
    }

    @Test
    void updateAppliesProvidedFieldsAndKeepsExistingOnesOtherwise() {
        DoctorAvailability availability = mock(DoctorAvailability.class);
        when(availability.isActive()).thenReturn(true);
        when(availability.getDayOfWeek()).thenReturn((short) 3);
        when(availability.getStartTime()).thenReturn(LocalTime.of(8, 0));
        when(availability.getEndTime()).thenReturn(LocalTime.of(16, 0));
        when(availability.getEffectiveFrom()).thenReturn(LocalDate.of(2026, 1, 1));
        when(availability.getEffectiveUntil()).thenReturn(null);
        when(availabilityRepository.findById(3L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(availability)).thenReturn(availability);

        UpdateDoctorAvailabilityRequest request = new UpdateDoctorAvailabilityRequest(
                null, LocalTime.of(10, 0), null, null, null);

        service.update(3L, request, 20L);

        verify(availability).setStartTime(LocalTime.of(10, 0));
        verify(availability).setEndTime(LocalTime.of(16, 0));
        verify(availability).setDayOfWeek((short) 3);
        verify(availability).setUpdatedByUserId(20L);
        verify(availabilityRepository).save(availability);
    }

    @Test
    void updateThrowsWhenMergedDayOfWeekOutOfRange() {
        DoctorAvailability availability = mock(DoctorAvailability.class);
        when(availability.isActive()).thenReturn(true);
        when(availability.getDayOfWeek()).thenReturn(null);
        when(availability.getStartTime()).thenReturn(LocalTime.of(8, 0));
        when(availability.getEndTime()).thenReturn(LocalTime.of(16, 0));
        when(availability.getEffectiveFrom()).thenReturn(LocalDate.of(2026, 1, 1));
        when(availabilityRepository.findById(3L)).thenReturn(Optional.of(availability));

        UpdateDoctorAvailabilityRequest request = new UpdateDoctorAvailabilityRequest(
                null, null, null, null, null);

        assertThatThrownBy(() -> service.update(3L, request, 20L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("dayOfWeek must be between 1 and 7");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenAvailabilityMissing() {
        when(availabilityRepository.findById(404L)).thenReturn(Optional.empty());

        UpdateDoctorAvailabilityRequest request = new UpdateDoctorAvailabilityRequest(
                null, null, null, null, null);

        assertThatThrownBy(() -> service.update(404L, request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void deleteDeactivatesAvailability() {
        DoctorAvailability availability = mock(DoctorAvailability.class);
        when(availability.isActive()).thenReturn(true);
        when(availabilityRepository.findById(7L)).thenReturn(Optional.of(availability));

        service.delete(7L, 42L);

        verify(availability).setActive(false);
        verify(availability).setUpdatedByUserId(42L);
        verify(availabilityRepository).save(availability);
    }

    @Test
    void deleteThrowsWhenAvailabilityMissing() {
        when(availabilityRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(availabilityRepository, never()).save(any());
    }
}