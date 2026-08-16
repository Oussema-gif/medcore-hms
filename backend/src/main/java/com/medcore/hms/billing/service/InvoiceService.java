package com.medcore.hms.billing.service;

import com.medcore.hms.billing.domain.Invoice;
import com.medcore.hms.billing.domain.InvoiceLineItem;
import com.medcore.hms.billing.domain.InvoiceStatus;
import com.medcore.hms.billing.dto.CreateInvoiceRequest;
import com.medcore.hms.billing.dto.InvoiceResponse;
import com.medcore.hms.billing.repository.InvoiceLineItemRepository;
import com.medcore.hms.billing.repository.InvoiceRepository;
import com.medcore.hms.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository lineItemRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceLineItemRepository lineItemRepository) {

        this.invoiceRepository = invoiceRepository;
        this.lineItemRepository = lineItemRepository;
    }

    public InvoiceResponse create(
            CreateInvoiceRequest input,
            Long actorUserId) {

        Invoice invoice = Invoice.create();

        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setPatientId(input.patientId());
        invoice.setAppointmentId(input.appointmentId());
        invoice.setCurrencyCode(
                input.currencyCode().trim().toUpperCase());
        invoice.setDueDate(input.dueDate());
        invoice.setCreatedByUserId(actorUserId);
        invoice.setUpdatedByUserId(actorUserId);

        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        Totals totals = Totals.zero();

        List<InvoiceLineItem> lineItems =
                input.lines()
                        .stream()
                        .map(line -> {
                            BigDecimal discount =
                                    money(line.discountAmount());

                            BigDecimal tax =
                                    money(line.taxAmount());

                            BigDecimal base =
                                    line.quantity()
                                            .multiply(line.unitPrice());

                            BigDecimal lineTotal =
                                    base.subtract(discount)
                                            .add(tax);

                            if (lineTotal.signum() < 0) {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invoice line total cannot be negative");
                            }

                            return createLine(
                                    savedInvoice.getId(),
                                    line,
                                    discount,
                                    tax,
                                    lineTotal);
                        })
                        .toList();

        for (InvoiceLineItem item : lineItems) {
            totals = totals.add(
                    item.getQuantity()
                            .multiply(item.getUnitPrice()),
                    item.getDiscountAmount(),
                    item.getTaxAmount());
        }

        lineItemRepository.saveAll(lineItems);

        invoice.setSubtotalAmount(totals.subtotal());
        invoice.setDiscountAmount(totals.discount());
        invoice.setTaxAmount(totals.tax());

        BigDecimal total =
                totals.subtotal()
                        .subtract(totals.discount())
                        .add(totals.tax());

        invoice.setTotalAmount(total);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(total);

        return toResponse(
                invoiceRepository.save(invoice),
                lineItems);
    }

    public InvoiceResponse issue(
            Long invoiceId,
            Long actorUserId) {

        Invoice invoice = getRequired(invoiceId);

        if (invoice.getStatus()
                != InvoiceStatus.DRAFT) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only a draft invoice can be issued");
        }

        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(Instant.now());
        invoice.setUpdatedByUserId(actorUserId);

        return toResponse(
                invoiceRepository.save(invoice),
                lineItemRepository
                        .findAllByInvoiceIdOrderByLineNumberAsc(
                                invoiceId));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(
            Long invoiceId) {

        Invoice invoice = getRequired(invoiceId);

        return toResponse(
                invoice,
                lineItemRepository
                        .findAllByInvoiceIdOrderByLineNumberAsc(
                                invoiceId));
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> findByPatientId(
            Long patientId,
            Pageable pageable) {

        return invoiceRepository
                .findAllByPatientId(patientId, pageable)
                .map(invoice ->
                        toResponse(
                                invoice,
                                lineItemRepository
                                        .findAllByInvoiceIdOrderByLineNumberAsc(
                                                invoice.getId())));
    }

    private InvoiceLineItem createLine(
            Long invoiceId,
            CreateInvoiceRequest.Line line,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal lineTotal) {

        InvoiceLineItem item =
                InvoiceLineItem.create();

        item.setInvoiceId(invoiceId);
        item.setLineNumber(line.lineNumber());
        item.setItemType(line.itemType().trim());
        item.setDescription(line.description().trim());
        item.setQuantity(line.quantity());
        item.setUnitPrice(line.unitPrice());
        item.setDiscountAmount(discount);
        item.setTaxAmount(tax);
        item.setLineTotalAmount(lineTotal);

        return item;
    }

    private Invoice getRequired(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice",
                                invoiceId));
    }

    private InvoiceResponse toResponse(
            Invoice invoice,
            List<InvoiceLineItem> items) {

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getPatientId(),
                invoice.getAppointmentId(),
                invoice.getStatus(),
                invoice.getCurrencyCode(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getSubtotalAmount(),
                invoice.getDiscountAmount(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getBalanceAmount(),
                invoice.getIssuedAt(),
                invoice.getVoidedAt(),
                invoice.getVoidReason(),
                items.stream()
                        .map(item ->
                                new InvoiceResponse.Line(
                                        item.getId(),
                                        item.getLineNumber(),
                                        item.getItemType(),
                                        item.getDescription(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getDiscountAmount(),
                                        item.getTaxAmount(),
                                        item.getLineTotalAmount()))
                        .toList());
    }

    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private BigDecimal money(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(4)
                : value.setScale(4, RoundingMode.HALF_UP);
    }

    private record Totals(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax) {

        static Totals zero() {
            return new Totals(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);
        }

        Totals add(
                BigDecimal subtotalValue,
                BigDecimal discountValue,
                BigDecimal taxValue) {

            return new Totals(
                    subtotal.add(subtotalValue),
                    discount.add(discountValue),
                    tax.add(taxValue));
        }
    }
}
