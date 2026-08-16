package com.medcore.hms.vitalsign.repository;

import com.medcore.hms.vitalsign.domain.VitalSign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VitalSignRepository
        extends JpaRepository<VitalSign, Long> {

    Page<VitalSign> findAllByMedicalRecordId(
            Long medicalRecordId,
            Pageable pageable);
}
