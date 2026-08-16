package com.medcore.hms.billing.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.billing.dto.CreateInvoiceRequest;
import com.medcore.hms.billing.dto.InvoiceResponse;
import com.medcore.hms.billing.service.InvoiceService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
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
@RequestMapping("/api/v1/invoices")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RECEPTIONIST')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(
            InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        InvoiceResponse response =
                invoiceService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Invoice created successfully"));
    }

    @PostMapping("/{invoiceId}/issue")
    public ApiResponse<InvoiceResponse> issue(
            @PathVariable Long invoiceId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                invoiceService.issue(
                        invoiceId,
                        authenticatedUser.getUserAccount().getId()),
                "Invoice issued successfully");
    }

    @GetMapping("/{invoiceId}")
    public ApiResponse<InvoiceResponse> getById(
            @PathVariable Long invoiceId) {

        return ApiResponse.success(
                invoiceService.getById(invoiceId),
                "Invoice retrieved successfully");
    }

    @GetMapping("/patient/{patientId}")
    public PagedApiResponse<InvoiceResponse> findByPatientId(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "issueDate")
            Pageable pageable) {

        return PagedApiResponse.from(
                invoiceService.findByPatientId(
                        patientId,
                        pageable),
                "Patient invoices retrieved successfully");
    }
}