package com.medcore.hms.laboratory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "laboratory_request_item")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaboratoryRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "laboratory_request_id", nullable = false)
    private Long laboratoryRequestId;

    @Column(name = "test_code", nullable = false, length = 64)
    private String testCode;

    @Column(name = "test_name", nullable = false, length = 256)
    private String testName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

    public static LaboratoryRequestItem create() {
        return new LaboratoryRequestItem();
    }
}
