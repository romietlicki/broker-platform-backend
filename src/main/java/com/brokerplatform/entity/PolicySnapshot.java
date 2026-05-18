package com.brokerplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "policy_snapshot",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_broker_external",
                columnNames = {"broker_id", "external_proposal_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_id", nullable = false)
    private Broker broker;

    @Column(name = "external_proposal_id", nullable = false, length = 100)
    private String externalProposalId;

    @Column(name = "insured_name", nullable = false, length = 150)
    private String insuredName;

    @Column(name = "insured_cpf", length = 14)
    private String insuredCpf;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "due_dates", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> dueDates;

    @Column(name = "coverage_description", columnDefinition = "TEXT")
    private String coverageDescription;

    @Column(name = "insured_capital", precision = 15, scale = 2)
    private BigDecimal insuredCapital;

    @Column(name = "premium_total", precision = 15, scale = 2)
    private BigDecimal premiumTotal;

    @Column(name = "payment_periodicity", length = 30)
    private String paymentPeriodicity;

    @Column(name = "insurer", length = 30)
    private String insurer;

    @Column(name = "product_name", length = 150)
    private String productName;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    @UpdateTimestamp
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
}
