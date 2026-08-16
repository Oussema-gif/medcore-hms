package com.medcore.hms.laboratory.repository;

import com.medcore.hms.laboratory.domain.LaboratoryRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaboratoryRequestItemRepository
        extends JpaRepository<LaboratoryRequestItem, Long> {

    List<LaboratoryRequestItem>
    findAllByLaboratoryRequestIdOrderBySortOrder(
            Long laboratoryRequestId);
}