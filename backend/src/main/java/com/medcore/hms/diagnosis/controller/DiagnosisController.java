package com.medcore.hms.diagnosis.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.diagnosis.dto.CreateDiagnosisRequest;
import com.medcore.hms.diagnosis.dto.DiagnosisResponse;
import com.medcore.hms.diagnosis.service.DiagnosisService;
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
@RequestMapping("/api/v1/diagnoses")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR')")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(
            DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiagnosisResponse>> create(
            @Valid @RequestBody CreateDiagnosisRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        DiagnosisResponse response =
                diagnosisService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Diagnosis created successfully"));
    }

    @GetMapping("/{diagnosisId}")
    public ApiResponse<DiagnosisResponse> getById(
            @PathVariable Long diagnosisId) {

        return ApiResponse.success(
                diagnosisService.getById(diagnosisId),
                "Diagnosis retrieved successfully");
    }

    @GetMapping("/medical-record/{medicalRecordId}")
    public PagedApiResponse<DiagnosisResponse> findByMedicalRecordId(
            @PathVariable Long medicalRecordId,
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                diagnosisService.findByMedicalRecordId(
                        medicalRecordId,
                        pageable),
                "Medical record diagnoses retrieved successfully");
    }
}