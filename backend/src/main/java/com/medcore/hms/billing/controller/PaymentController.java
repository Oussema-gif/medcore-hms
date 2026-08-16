package com.medcore.hms.billing.controller;

import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.service.PaymentService;
import com.medcore.hms.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'RECEPTIONIST')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> record(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        paymentService.record(
                                request,
                                principal.getUserAccount().getId()),
                        "Payment recorded successfully"));
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getById(
            @PathVariable Long paymentId) {

        return ApiResponse.success(
                paymentService.getById(paymentId),
                "Payment retrieved successfully");
    }

    @GetMapping("/invoice/{invoiceId}")
    public ApiResponse<List<PaymentResponse>>
    findByInvoiceId(@PathVariable Long invoiceId) {

        return ApiResponse.success(
                paymentService.findByInvoiceId(invoiceId),
                "Invoice payments retrieved successfully");
    }

    @PostMapping("/{paymentId}/void")
    public ApiResponse<PaymentResponse> voidPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody VoidPaymentRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal principal) {

        return ApiResponse.success(
                paymentService.voidPayment(
                        paymentId,
                        request,
                        principal.getUserAccount().getId()),
                "Payment voided successfully");
    }
}
