package com.medcore.hms.laboratory.repository;

import com.medcore.hms.laboratory.domain.LaboratoryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaboratoryResultRepository
        extends JpaRepository<LaboratoryResult, Long> {

    List<LaboratoryResult>
    findAllByLaboratoryRequestItemIdOrderByRecordedAtDesc(
            Long laboratoryRequestItemId);

    long countByLaboratoryRequestItemId(
            Long laboratoryRequestItemId);

    long countByLaboratoryRequestItemIdAndReleasedAtIsNotNull(
            Long laboratoryRequestItemId);
}