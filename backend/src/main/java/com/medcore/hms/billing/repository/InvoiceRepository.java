package com.medcore.hms.billing.repository;

import com.medcore.hms.billing.domain.Invoice;
import com.medcore.hms.billing.domain.InvoiceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository
        extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(
            String invoiceNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    Page<Invoice> findAllByPatientId(
            Long patientId,
            Pageable pageable);

    Page<Invoice> findAllByPatientIdAndStatus(
            Long patientId,
            InvoiceStatus status,
            Pageable pageable);
}