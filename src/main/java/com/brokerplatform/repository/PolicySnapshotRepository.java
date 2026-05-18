package com.brokerplatform.repository;

import com.brokerplatform.entity.PolicySnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicySnapshotRepository extends JpaRepository<PolicySnapshot, Long> {

    Page<PolicySnapshot> findByBrokerId(Long brokerId, Pageable pageable);

    @Query("SELECT p FROM PolicySnapshot p WHERE p.broker.id = :brokerId " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:insuredName IS NULL OR LOWER(p.insuredName) LIKE LOWER(CONCAT('%', :insuredName, '%')))")
    Page<PolicySnapshot> findByBrokerIdWithFilters(
            @Param("brokerId") Long brokerId,
            @Param("status") String status,
            @Param("insuredName") String insuredName,
            Pageable pageable);

    Optional<PolicySnapshot> findByBrokerIdAndExternalProposalId(Long brokerId, String externalProposalId);

    @Query(value = "SELECT p.* FROM policy_snapshot p " +
            "WHERE p.broker_id = :brokerId " +
            "AND JSON_OVERLAPS(p.due_dates, JSON_ARRAY(:today)) = 1 " +
            "AND p.status NOT IN ('CANCELADA', 'CANCELADO')",
            nativeQuery = true)
    List<PolicySnapshot> findOverduePolicies(
            @Param("brokerId") Long brokerId,
            @Param("today") String today);

    @Query("SELECT p FROM PolicySnapshot p WHERE p.broker.id = :brokerId " +
           "AND p.status NOT IN ('CANCELADA', 'CANCELADO') " +
           "AND p.endDate IS NOT NULL AND p.endDate <= :limitDate")
    List<PolicySnapshot> findExpiringPolicies(
            @Param("brokerId") Long brokerId,
            @Param("limitDate") LocalDate limitDate);

    List<PolicySnapshot> findByBrokerIdAndInsurer(Long brokerId, String insurer);
}
