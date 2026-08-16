package com.medcore.hms.doctoravailability.domain;

import com.medcore.hms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "doctor_availability")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DoctorAvailability extends BaseEntity {

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "day_of_week", nullable = false)
    private Short dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_until")
    private LocalDate effectiveUntil;

    @Column(nullable = false)
    private boolean active = true;

    public static DoctorAvailability create() {
        return new DoctorAvailability();
    }
}