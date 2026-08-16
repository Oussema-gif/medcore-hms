package com.medcore.hms.vitalsign.controller;

import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.vitalsign.dto.CreateVitalSignRequest;
import com.medcore.hms.vitalsign.dto.VitalSignResponse;
import com.medcore.hms.vitalsign.service.VitalSignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vital-signs")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'NURSE')")
public class VitalSignController {

    private final VitalSignService vitalSignService;

    public VitalSignController(
            VitalSignService vitalSignService) {
        this.vitalSignService = vitalSignService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VitalSignResponse>> create(
            @Valid @RequestBody CreateVitalSignRequest request) {

        VitalSignResponse response =
                vitalSignService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Vital signs recorded successfully"));
    }

    @GetMapping("/{vitalSignId}")
    public ApiResponse<VitalSignResponse> getById(
            @PathVariable Long vitalSignId) {

        return ApiResponse.success(
                vitalSignService.getById(vitalSignId),
                "Vital signs retrieved successfully");
    }

    @GetMapping("/medical-record/{medicalRecordId}")
    public PagedApiResponse<VitalSignResponse> findByMedicalRecordId(
            @PathVariable Long medicalRecordId,
            @PageableDefault(size = 20, sort = "recordedAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                vitalSignService.findByMedicalRecordId(
                        medicalRecordId,
                        pageable),
                "Medical record vital signs retrieved successfully");
    }
}