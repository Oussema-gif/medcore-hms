package com.medcore.hms.laboratory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "laboratory_result")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaboratoryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "laboratory_request_item_id", nullable = false)
    private Long laboratoryRequestItemId;

    @Column(name = "result_value", precision = 19, scale = 6)
    private BigDecimal resultValue;

    @Column(name = "result_text")
    private String resultText;

    @Column(length = 64)
    private String unit;

    @Column(name = "reference_range", length = 256)
    private String referenceRange;

    @Column(length = 1024)
    private String interpretation;

    @Column(name = "recorded_by_user_id", nullable = false)
    private Long recordedByUserId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "amended_from_result_id")
    private Long amendedFromResultId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static LaboratoryResult create() {
        Instant now = Instant.now();

        LaboratoryResult result = new LaboratoryResult();
        result.recordedAt = now;
        result.createdAt = now;
        result.updatedAt = now;

        return result;
    }
}
