package com.medcore.hms.laboratory.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.laboratory.dto.CreateLaboratoryResultRequest;
import com.medcore.hms.laboratory.dto.LaboratoryResultResponse;
import com.medcore.hms.laboratory.service.LaboratoryResultService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/laboratory-results")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'NURSE')")
public class LaboratoryResultController {

    private final LaboratoryResultService laboratoryResultService;

    public LaboratoryResultController(
            LaboratoryResultService laboratoryResultService) {
        this.laboratoryResultService = laboratoryResultService;
    }

    @PostMapping
    public ApiResponse<LaboratoryResultResponse> create(
            @Valid @RequestBody CreateLaboratoryResultRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {

        return ApiResponse.success(
                laboratoryResultService.create(
                        request,
                        principal.getUserAccount().getId()),
                "Laboratory result recorded successfully");
    }

    @GetMapping("/{resultId}")
    public ApiResponse<LaboratoryResultResponse> getById(
            @PathVariable Long resultId) {

        return ApiResponse.success(
                laboratoryResultService.getById(resultId),
                "Laboratory result retrieved successfully");
    }

    @GetMapping("/request-item/{requestItemId}")
    public ApiResponse<List<LaboratoryResultResponse>>
    findByRequestItemId(
            @PathVariable Long requestItemId) {

        return ApiResponse.success(
                laboratoryResultService.findByRequestItemId(
                        requestItemId),
                "Laboratory results retrieved successfully");
    }

    @PostMapping("/{resultId}/release")
    public ApiResponse<LaboratoryResultResponse> release(
            @PathVariable Long resultId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {
        return ApiResponse.success(
                laboratoryResultService.release(
                        resultId,
                        principal.getUserAccount().getId()),
                "Laboratory result released successfully");
    }
}