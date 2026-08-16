package com.medcore.hms.doctoravailability.service;

import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctoravailability.domain.DoctorAvailability;
import com.medcore.hms.doctoravailability.dto.CreateDoctorAvailabilityRequest;
import com.medcore.hms.doctoravailability.dto.DoctorAvailabilityResponse;
import com.medcore.hms.doctoravailability.dto.UpdateDoctorAvailabilityRequest;
import com.medcore.hms.doctoravailability.repository.DoctorAvailabilityRepository;
import com.medcore.hms.exception.BadRequestException;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;

    public DoctorAvailabilityService(
            DoctorAvailabilityRepository availabilityRepository,
            DoctorRepository doctorRepository) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
    }

    public DoctorAvailabilityResponse create(
            CreateDoctorAvailabilityRequest request,
            Long actorUserId) {

        getRequiredDoctor(request.doctorId());
        validateTimeRange(request.startTime(), request.endTime());
        validateDateRange(request.effectiveFrom(), request.effectiveUntil());

        DoctorAvailability availability = DoctorAvailability.create();

        availability.setDoctorId(request.doctorId());
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setEffectiveFrom(request.effectiveFrom());
        availability.setEffectiveUntil(request.effectiveUntil());
        availability.setActive(true);
        availability.setCreatedByUserId(actorUserId);
        availability.setUpdatedByUserId(actorUserId);

        return toResponse(availabilityRepository.save(availability));
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityResponse getById(Long availabilityId) {
        return toResponse(getRequiredAvailability(availabilityId));
    }

    @Transactional(readOnly = true)
    public Page<DoctorAvailabilityResponse> findByDoctorId(
            Long doctorId,
            Pageable pageable) {

        getRequiredDoctor(doctorId);

        return availabilityRepository
                .findAllByDoctorIdAndActiveTrue(doctorId, pageable)
                .map(this::toResponse);
    }

    public DoctorAvailabilityResponse update(
            Long availabilityId,
            UpdateDoctorAvailabilityRequest request,
            Long actorUserId) {

        DoctorAvailability availability =
                getRequiredAvailability(availabilityId);

        Short dayOfWeek = request.dayOfWeek() != null
                ? request.dayOfWeek()
                : availability.getDayOfWeek();

        var startTime = request.startTime() != null
                ? request.startTime()
                : availability.getStartTime();

        var endTime = request.endTime() != null
                ? request.endTime()
                : availability.getEndTime();

        var effectiveFrom = request.effectiveFrom() != null
                ? request.effectiveFrom()
                : availability.getEffectiveFrom();

        var effectiveUntil = request.effectiveUntil() != null
                ? request.effectiveUntil()
                : availability.getEffectiveUntil();

        validateDayOfWeek(dayOfWeek);
        validateTimeRange(startTime, endTime);
        validateDateRange(effectiveFrom, effectiveUntil);

        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setEffectiveFrom(effectiveFrom);
        availability.setEffectiveUntil(effectiveUntil);
        availability.setUpdatedByUserId(actorUserId);

        return toResponse(availabilityRepository.save(availability));
    }

    public void delete(Long availabilityId, Long actorUserId) {
        DoctorAvailability availability =
                getRequiredAvailability(availabilityId);

        availability.setActive(false);
        availability.setUpdatedByUserId(actorUserId);
        availabilityRepository.save(availability);
    }

    private com.medcore.hms.doctor.domain.Doctor getRequiredDoctor(
            Long doctorId) {
        return doctorRepository.findByIdAndActiveTrue(doctorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor", doctorId));
    }

    private DoctorAvailability getRequiredAvailability(
            Long availabilityId) {
        return availabilityRepository.findById(availabilityId)
                .filter(DoctorAvailability::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor availability",
                                availabilityId));
    }

    private void validateDayOfWeek(Short dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            throw new BadRequestException(
                    "dayOfWeek must be between 1 and 7");
        }
    }

    private void validateTimeRange(
            java.time.LocalTime startTime,
            java.time.LocalTime endTime) {

        if (startTime == null || endTime == null
                || !startTime.isBefore(endTime)) {
            throw new BadRequestException(
                    "startTime must be before endTime");
        }
    }

    private void validateDateRange(
            java.time.LocalDate effectiveFrom,
            java.time.LocalDate effectiveUntil) {

        if (effectiveFrom == null
                || (effectiveUntil != null
                && effectiveUntil.isBefore(effectiveFrom))) {
            throw new BadRequestException(
                    "effectiveUntil must be on or after effectiveFrom");
        }
    }

    private DoctorAvailabilityResponse toResponse(
            DoctorAvailability availability) {

        return new DoctorAvailabilityResponse(
                availability.getId(),
                availability.getDoctorId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getEffectiveFrom(),
                availability.getEffectiveUntil(),
                availability.isActive(),
                availability.getCreatedAt(),
                availability.getUpdatedAt());
    }
}