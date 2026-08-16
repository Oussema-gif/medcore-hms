package com.medcore.hms.inventory.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.inventory.domain.StockMovement;
import com.medcore.hms.inventory.dto.AdjustInventoryRequest;
import com.medcore.hms.inventory.dto.InventoryBatchResponse;
import com.medcore.hms.inventory.dto.ReceiveInventoryBatchRequest;
import com.medcore.hms.inventory.service.InventoryBatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory-batches")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RECEPTIONIST')")
public class InventoryBatchController {

    private final InventoryBatchService inventoryBatchService;

    public InventoryBatchController(
            InventoryBatchService inventoryBatchService) {

        this.inventoryBatchService = inventoryBatchService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryBatchResponse>> receive(
            @Valid @RequestBody ReceiveInventoryBatchRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        inventoryBatchService.receive(
                                request,
                                principal.getUserAccount().getId()),
                        "Inventory batch received successfully"));
    }

    @GetMapping("/medicine/{medicineId}")
    public ApiResponse<List<InventoryBatchResponse>>
    findAvailableByMedicineId(
            @PathVariable Long medicineId) {

        return ApiResponse.success(
                inventoryBatchService
                        .findAvailableByMedicineId(medicineId),
                "Available inventory batches retrieved successfully");
    }

    @GetMapping("/{batchId}/movements")
    public ApiResponse<List<StockMovement>> findMovements(
            @PathVariable Long batchId) {

        return ApiResponse.success(
                inventoryBatchService.findMovements(batchId),
                "Stock movements retrieved successfully");
    }

    @PostMapping("/{batchId}/adjust")
    public ApiResponse<InventoryBatchResponse> adjust(
            @PathVariable Long batchId,
            @Valid @RequestBody AdjustInventoryRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {
        return ApiResponse.success(
                inventoryBatchService.adjust(
                        batchId,
                        request,
                        principal.getUserAccount().getId()),
                "Inventory batch adjusted successfully");
    }
}