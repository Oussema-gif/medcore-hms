package com.medcore.hms.prescription.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.prescription.dto.CancelPrescriptionRequest;
import com.medcore.hms.prescription.dto.CreatePrescriptionRequest;
import com.medcore.hms.prescription.dto.PrescriptionResponse;
import com.medcore.hms.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prescriptions")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR')")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(
            PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(
            @Valid @RequestBody CreatePrescriptionRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        PrescriptionResponse response =
                prescriptionService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Prescription created successfully"));
    }

    @GetMapping("/{prescriptionId}")
    public ApiResponse<PrescriptionResponse> getById(
            @PathVariable Long prescriptionId) {

        return ApiResponse.success(
                prescriptionService.getById(prescriptionId),
                "Prescription retrieved successfully");
    }

    @GetMapping("/patient/{patientId}")
    public PagedApiResponse<PrescriptionResponse> findByPatientId(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "prescribedAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                prescriptionService.findByPatientId(
                        patientId,
                        pageable),
                "Patient prescriptions retrieved successfully");
    }

    @PostMapping("/{prescriptionId}/cancel")
    public ApiResponse<PrescriptionResponse> cancel(
            @PathVariable Long prescriptionId,
            @Valid @RequestBody CancelPrescriptionRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                prescriptionService.cancel(
                        prescriptionId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Prescription cancelled successfully");
    }

    @PostMapping("/{prescriptionId}/dispense")
    public ApiResponse<PrescriptionResponse> dispense(
            @PathVariable Long prescriptionId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                prescriptionService.dispense(
                        prescriptionId,
                        authenticatedUser.getUserAccount().getId()),
                "Prescription dispensed successfully");
    }
}