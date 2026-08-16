package com.medcore.hms.medicalrecord.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequest;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponse;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequest;
import com.medcore.hms.medicalrecord.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medical-records")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR')")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(
            MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> create(
            @Valid @RequestBody CreateMedicalRecordRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        MedicalRecordResponse response =
                medicalRecordService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Medical record created successfully"));
    }

    @GetMapping("/{medicalRecordId}")
    public ApiResponse<MedicalRecordResponse> getById(
            @PathVariable Long medicalRecordId) {

        return ApiResponse.success(
                medicalRecordService.getById(medicalRecordId),
                "Medical record retrieved successfully");
    }

    @GetMapping("/patient/{patientId}")
    public PagedApiResponse<MedicalRecordResponse> findByPatientId(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "encounterAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                medicalRecordService.findByPatientId(
                        patientId,
                        pageable),
                "Patient medical records retrieved successfully");
    }

    @GetMapping("/doctor/{doctorId}")
    public PagedApiResponse<MedicalRecordResponse> findByDoctorId(
            @PathVariable Long doctorId,
            @PageableDefault(size = 20, sort = "encounterAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                medicalRecordService.findByDoctorId(
                        doctorId,
                        pageable),
                "Doctor medical records retrieved successfully");
    }

    @PatchMapping("/{medicalRecordId}")
    public ApiResponse<MedicalRecordResponse> update(
            @PathVariable Long medicalRecordId,
            @Valid @RequestBody UpdateMedicalRecordRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                medicalRecordService.update(
                        medicalRecordId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Medical record updated successfully");
    }

    @PostMapping("/{medicalRecordId}/release")
    public ApiResponse<MedicalRecordResponse> release(
            @PathVariable Long medicalRecordId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                medicalRecordService.release(
                        medicalRecordId,
                        authenticatedUser.getUserAccount().getId()),
                "Medical record released successfully");
    }
}