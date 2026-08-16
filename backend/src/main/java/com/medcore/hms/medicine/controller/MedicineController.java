package com.medcore.hms.medicine.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import com.medcore.hms.medicine.dto.CreateMedicineRequest;
import com.medcore.hms.medicine.dto.MedicineResponse;
import com.medcore.hms.medicine.dto.UpdateMedicineRequest;
import com.medcore.hms.medicine.service.MedicineService;
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
@RequestMapping("/api/v1/medicines")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'RECEPTIONIST')")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(
            MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponse>> create(
            @Valid @RequestBody CreateMedicineRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        MedicineResponse response =
                medicineService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Medicine created successfully"));
    }

    @GetMapping("/{medicineId}")
    public ApiResponse<MedicineResponse> getById(
            @PathVariable Long medicineId) {

        return ApiResponse.success(
                medicineService.getById(medicineId),
                "Medicine retrieved successfully");
    }

    @GetMapping
    public PagedApiResponse<MedicineResponse> findAll(
            @PageableDefault(size = 20, sort = "name")
            Pageable pageable) {

        return PagedApiResponse.from(
                medicineService.findAll(pageable),
                "Medicines retrieved successfully");
    }

    @PatchMapping("/{medicineId}")
    public ApiResponse<MedicineResponse> update(
            @PathVariable Long medicineId,
            @Valid @RequestBody UpdateMedicineRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                medicineService.update(
                        medicineId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Medicine updated successfully");
    }

    @DeleteMapping("/{medicineId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long medicineId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        medicineService.delete(
                medicineId,
                authenticatedUser.getUserAccount().getId());

        return ResponseEntity.noContent().build();
    }
}