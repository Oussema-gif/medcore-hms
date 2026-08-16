package com.medcore.hms.doctoravailability.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.doctoravailability.dto.CreateDoctorAvailabilityRequest;
import com.medcore.hms.doctoravailability.dto.DoctorAvailabilityResponse;
import com.medcore.hms.doctoravailability.dto.UpdateDoctorAvailabilityRequest;
import com.medcore.hms.doctoravailability.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doctor-availability")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'RECEPTIONIST')")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(
            DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> create(
            @Valid @RequestBody CreateDoctorAvailabilityRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        DoctorAvailabilityResponse response =
                availabilityService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Doctor availability created successfully"));
    }

    @GetMapping("/{availabilityId}")
    public ApiResponse<DoctorAvailabilityResponse> getById(
            @PathVariable Long availabilityId) {

        return ApiResponse.success(
                availabilityService.getById(availabilityId),
                "Doctor availability retrieved successfully");
    }

    @GetMapping("/doctor/{doctorId}")
    public PagedApiResponse<DoctorAvailabilityResponse> findByDoctorId(
            @PathVariable Long doctorId,
            @PageableDefault(size = 20, sort = "dayOfWeek")
            Pageable pageable) {

        return PagedApiResponse.from(
                availabilityService.findByDoctorId(
                        doctorId,
                        pageable),
                "Doctor availability retrieved successfully");
    }

    @PatchMapping("/{availabilityId}")
    public ApiResponse<DoctorAvailabilityResponse> update(
            @PathVariable Long availabilityId,
            @Valid @RequestBody UpdateDoctorAvailabilityRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                availabilityService.update(
                        availabilityId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Doctor availability updated successfully");
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long availabilityId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        availabilityService.delete(
                availabilityId,
                authenticatedUser.getUserAccount().getId());

        return ResponseEntity.noContent().build();
    }
}