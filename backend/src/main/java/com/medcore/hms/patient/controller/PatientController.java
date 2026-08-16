package com.medcore.hms.patient.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.patient.dto.CreatePatientRequest;
import com.medcore.hms.patient.dto.PatientResponse;
import com.medcore.hms.patient.dto.UpdatePatientRequest;
import com.medcore.hms.patient.service.PatientService;
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
@RequestMapping("/api/v1/patients")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'RECEPTIONIST')")
public class PatientController {

    private final PatientService patientService;

    public PatientController(
            PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> create(
            @Valid @RequestBody CreatePatientRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        PatientResponse response =
                patientService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Patient created successfully"));
    }

    @GetMapping("/{patientId}")
    public ApiResponse<PatientResponse> getById(
            @PathVariable Long patientId) {

        return ApiResponse.success(
                patientService.getById(patientId),
                "Patient retrieved successfully");
    }

    @GetMapping
    public PagedApiResponse<PatientResponse> findAll(
            @PageableDefault(size = 20, sort = "lastName")
            Pageable pageable) {

        return PagedApiResponse.from(
                patientService.findAll(pageable),
                "Patients retrieved successfully");
    }

    @PatchMapping("/{patientId}")
    public ApiResponse<PatientResponse> update(
            @PathVariable Long patientId,
            @Valid @RequestBody UpdatePatientRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                patientService.update(
                        patientId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Patient updated successfully");
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long patientId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        patientService.delete(
                patientId,
                authenticatedUser.getUserAccount().getId());

        return ResponseEntity.noContent().build();
    }
}