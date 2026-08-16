package com.medcore.hms.billing.service;

import com.medcore.hms.billing.domain.*;
import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.repository.InvoiceRepository;
import com.medcore.hms.billing.repository.PaymentRepository;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository) {

        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public PaymentResponse record(
            CreatePaymentRequest request,
            Long recordedByUserId) {

        Invoice invoice = invoiceRepository
                .findByIdForUpdate(request.invoiceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice",
                                request.invoiceId()));

        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw badRequest(
                    "Payments cannot be recorded for a draft invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw badRequest(
                    "Payments cannot be recorded for a void invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw badRequest(
                    "Invoice is already fully paid");
        }

        BigDecimal amount = money(request.amount());
        BigDecimal balance = money(invoice.getBalanceAmount());

        if (amount.compareTo(balance) > 0) {
            throw badRequest(
                    "Payment amount cannot exceed invoice balance");
        }

        Payment payment = Payment.create();

        payment.setPaymentNumber(generatePaymentNumber());
        payment.setInvoiceId(invoice.getId());
        payment.setPatientId(invoice.getPatientId());
        payment.setAmount(amount);
        payment.setCurrencyCode(invoice.getCurrencyCode());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentReference(
                blankToNull(request.paymentReference()));
        payment.setPaidAt(Instant.now());
        payment.setRecordedByUserId(recordedByUserId);
        payment.setStatus(PaymentStatus.RECORDED);

        Payment saved = paymentRepository.save(payment);

        BigDecimal paidAmount =
                money(invoice.getPaidAmount())
                        .add(amount);

        BigDecimal newBalance =
                money(invoice.getTotalAmount())
                        .subtract(paidAmount);

        invoice.setPaidAmount(paidAmount);
        invoice.setBalanceAmount(newBalance);
        invoice.setStatus(
                newBalance.signum() == 0
                        ? InvoiceStatus.PAID
                        : InvoiceStatus.PARTIALLY_PAID);
        invoice.setUpdatedByUserId(recordedByUserId);

        invoiceRepository.save(invoice);

        return toResponse(saved);
    }

    public PaymentResponse voidPayment(
            Long paymentId,
            VoidPaymentRequest request,
            Long actorUserId) {

        Payment payment = paymentRepository
                .findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment",
                                paymentId));

        if (payment.getStatus() == PaymentStatus.VOIDED) {
            throw badRequest("Payment is already voided");
        }

        Invoice invoice = invoiceRepository
                .findByIdForUpdate(payment.getInvoiceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice",
                                payment.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw badRequest(
                    "Payments cannot be voided on a void invoice");
        }

        BigDecimal paidAmount =
                money(invoice.getPaidAmount())
                        .subtract(payment.getAmount());

        BigDecimal balance =
                money(invoice.getTotalAmount())
                        .subtract(paidAmount);

        payment.setStatus(PaymentStatus.VOIDED);
        payment.setVoidedAt(Instant.now());
        payment.setVoidReason(request.reason().trim());

        invoice.setPaidAmount(paidAmount);
        invoice.setBalanceAmount(balance);
        invoice.setStatus(
                paidAmount.signum() == 0
                        ? InvoiceStatus.ISSUED
                        : InvoiceStatus.PARTIALLY_PAID);
        invoice.setUpdatedByUserId(actorUserId);

        invoiceRepository.save(invoice);
        paymentRepository.save(payment);

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Long paymentId) {
        return toResponse(
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment",
                                        paymentId)));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByInvoiceId(
            Long invoiceId) {

        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException(
                    "Invoice",
                    invoiceId);
        }

        return paymentRepository
                .findAllByInvoiceIdOrderByPaidAtDesc(invoiceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getInvoiceId(),
                payment.getPatientId(),
                payment.getAmount(),
                payment.getCurrencyCode(),
                payment.getPaymentMethod(),
                payment.getPaymentReference(),
                payment.getPaidAt(),
                payment.getRecordedByUserId(),
                payment.getStatus(),
                payment.getVoidedAt(),
                payment.getVoidReason());
    }

    private String generatePaymentNumber() {
        return "PAY-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private BigDecimal money(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(4)
                : value.setScale(4, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private ResponseStatusException badRequest(
            String message) {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }
}
