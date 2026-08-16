package com.medcore.hms.doctor.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.doctor.dto.CreateDoctorRequest;
import com.medcore.hms.doctor.dto.DoctorResponse;
import com.medcore.hms.doctor.dto.UpdateDoctorRequest;
import com.medcore.hms.doctor.service.DoctorService;
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
@RequestMapping("/api/v1/doctors")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'RECEPTIONIST')")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(
            DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorResponse>> create(
            @Valid @RequestBody CreateDoctorRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        DoctorResponse response =
                doctorService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Doctor created successfully"));
    }

    @GetMapping("/{doctorId}")
    public ApiResponse<DoctorResponse> getById(
            @PathVariable Long doctorId) {

        return ApiResponse.success(
                doctorService.getById(doctorId),
                "Doctor retrieved successfully");
    }

    @GetMapping
    public PagedApiResponse<DoctorResponse> findAll(
            @PageableDefault(size = 20, sort = "lastName")
            Pageable pageable) {

        return PagedApiResponse.from(
                doctorService.findAll(pageable),
                "Doctors retrieved successfully");
    }

    @PatchMapping("/{doctorId}")
    public ApiResponse<DoctorResponse> update(
            @PathVariable Long doctorId,
            @Valid @RequestBody UpdateDoctorRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                doctorService.update(
                        doctorId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Doctor updated successfully");
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long doctorId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        doctorService.delete(
                doctorId,
                authenticatedUser.getUserAccount().getId());

        return ResponseEntity.noContent().build();
    }
}