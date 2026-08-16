package com.medcore.hms.laboratory.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.laboratory.dto.CancelLaboratoryRequest;
import com.medcore.hms.laboratory.dto.CreateLaboratoryRequest;
import com.medcore.hms.laboratory.dto.LaboratoryRequestResponse;
import com.medcore.hms.laboratory.service.LaboratoryRequestService;
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
@RequestMapping("/api/v1/laboratory-requests")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'NURSE')")
public class LaboratoryRequestController {

    private final LaboratoryRequestService laboratoryRequestService;

    public LaboratoryRequestController(
            LaboratoryRequestService laboratoryRequestService) {
        this.laboratoryRequestService = laboratoryRequestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LaboratoryRequestResponse>> create(
            @Valid @RequestBody CreateLaboratoryRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        LaboratoryRequestResponse response =
                laboratoryRequestService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Laboratory request created successfully"));
    }

    @PostMapping("/{requestId}/cancel")
    public ApiResponse<LaboratoryRequestResponse> cancel(
            @PathVariable Long requestId,
            @Valid @RequestBody CancelLaboratoryRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                laboratoryRequestService.cancel(
                        requestId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Laboratory request cancelled successfully");
    }

    @GetMapping("/{requestId}")
    public ApiResponse<LaboratoryRequestResponse> getById(
            @PathVariable Long requestId) {

        return ApiResponse.success(
                laboratoryRequestService.getById(requestId),
                "Laboratory request retrieved successfully");
    }

    @GetMapping("/patient/{patientId}")
    public PagedApiResponse<LaboratoryRequestResponse> findByPatientId(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "requestedAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                laboratoryRequestService.findByPatientId(
                        patientId,
                        pageable),
                "Patient laboratory requests retrieved successfully");
    }
}