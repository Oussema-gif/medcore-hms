package com.medcore.hms.prescription.repository;

import com.medcore.hms.prescription.domain.Prescription;
import com.medcore.hms.prescription.domain.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository
        extends JpaRepository<Prescription, Long> {

    Page<Prescription> findAllByPatientId(
            Long patientId,
            Pageable pageable);

    Page<Prescription> findAllByPatientIdAndStatus(
            Long patientId,
            PrescriptionStatus status,
            Pageable pageable);
}
