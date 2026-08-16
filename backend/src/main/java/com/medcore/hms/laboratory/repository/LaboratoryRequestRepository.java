package com.medcore.hms.laboratory.repository;

import com.medcore.hms.laboratory.domain.LaboratoryRequest;
import com.medcore.hms.laboratory.domain.LaboratoryRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface LaboratoryRequestRepository
        extends JpaRepository<LaboratoryRequest, Long> {

    Page<LaboratoryRequest> findAllByPatientId(
            Long patientId,
            Pageable pageable);

    @Query("""
           select count(r) from LaboratoryRequest r
           where r.status not in :excludedStatuses
           """)
    long countPendingRequests(
            @Param("excludedStatuses") Collection<LaboratoryRequestStatus> excludedStatuses);

    long countByStatusNotIn(
            Collection<LaboratoryRequestStatus> statuses);
}