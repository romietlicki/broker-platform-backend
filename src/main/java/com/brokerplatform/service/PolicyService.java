package com.brokerplatform.service;

import com.brokerplatform.dto.response.DelinquencyResponse;
import com.brokerplatform.dto.response.PolicyResponse;
import com.brokerplatform.entity.Broker;
import com.brokerplatform.entity.PolicySnapshot;
import com.brokerplatform.exception.ResourceNotFoundException;
import com.brokerplatform.repository.BrokerRepository;
import com.brokerplatform.repository.PolicySnapshotRepository;
import com.brokerplatform.service.insurer.InsurerService;
import com.brokerplatform.service.insurer.InsurerServiceFactory;
import com.brokerplatform.service.insurer.InsurerType;
import com.brokerplatform.service.insurer.PolicyData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicySnapshotRepository policyRepo;
    private final BrokerRepository brokerRepository;
    private final InsurerServiceFactory insurerFactory;

    @Transactional(readOnly = true)
    public Page<PolicyResponse> listPolicies(Long brokerId, String status, String insuredName,
                                             int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PolicySnapshot> result = policyRepo.findByBrokerIdWithFilters(brokerId, status, insuredName, pageable);
        return result.map(PolicyResponse::from);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicy(Long brokerId, Long policyId) {
        PolicySnapshot snapshot = policyRepo.findById(policyId)
                .filter(p -> p.getBroker().getId().equals(brokerId))
                .orElseThrow(() -> new ResourceNotFoundException("Apólice", policyId));
        return PolicyResponse.from(snapshot);
    }

    @Transactional(readOnly = true)
    public List<DelinquencyResponse> listDelinquencies(Long brokerId) {
        String today = LocalDate.now().toString();
        List<PolicySnapshot> overdue = policyRepo.findOverduePolicies(brokerId, today);

        return overdue.stream().map(p -> {
            String overdueDate = p.getDueDates() == null ? null :
                    p.getDueDates().stream()
                            .filter(d -> d.compareTo(today) <= 0)
                            .findFirst()
                            .orElse(null);

            return new DelinquencyResponse(
                    p.getId(),
                    p.getExternalProposalId(),
                    p.getInsuredName(),
                    p.getInsuredCpf(),
                    p.getPremiumTotal(),
                    overdueDate,
                    p.getInsuredCapital(),
                    p.getInsurer(),
                    p.getProductName(),
                    p.getStatus()
            );
        }).toList();
    }

    @Transactional
    public int syncAllPolicies(Long brokerId) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new ResourceNotFoundException("Corretor", brokerId));

        List<InsurerService> services = insurerFactory.getAll();
        List<PolicySnapshot> synced = new ArrayList<>();

        for (InsurerService svc : services) {
            try {
                List<PolicyData> policies = svc.fetchPolicies(broker.getCpf(), null);
                for (PolicyData pd : policies) {
                    PolicySnapshot snapshot = policyRepo
                            .findByBrokerIdAndExternalProposalId(brokerId, pd.externalProposalId())
                            .orElseGet(PolicySnapshot::new);

                    snapshot.setBroker(broker);
                    snapshot.setExternalProposalId(pd.externalProposalId());
                    snapshot.setInsuredName(pd.insuredName());
                    snapshot.setInsuredCpf(pd.insuredCpf());
                    snapshot.setStatus(pd.status());
                    snapshot.setStartDate(pd.startDate());
                    snapshot.setEndDate(pd.endDate());
                    snapshot.setDueDates(pd.dueDates());
                    snapshot.setCoverageDescription(pd.coverageDescription());
                    snapshot.setInsuredCapital(pd.insuredCapital());
                    snapshot.setPremiumTotal(pd.premiumTotal());
                    snapshot.setPaymentPeriodicity(pd.paymentPeriodicity());
                    snapshot.setInsurer(svc.getType().name());
                    snapshot.setProductName(pd.productName());
                    snapshot.setGracePeriodDays(pd.gracePeriodDays());
                    snapshot.setCancellationDate(pd.cancellationDate());

                    synced.add(snapshot);
                }
            } catch (Exception e) {
                log.error("[Sync] Erro ao sincronizar apólices de {} para broker {}: {}",
                        svc.getType(), brokerId, e.getMessage());
            }
        }

        policyRepo.saveAll(synced);
        log.info("[Sync] {} apólices sincronizadas para broker {}", synced.size(), brokerId);
        return synced.size();
    }

    @Transactional
    public int syncPoliciesForInsurer(Long brokerId, InsurerType type) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new ResourceNotFoundException("Corretor", brokerId));

        InsurerService svc = insurerFactory.get(type);
        List<PolicyData> policies = svc.fetchPolicies(broker.getCpf(), null);
        List<PolicySnapshot> synced = policies.stream().map(pd -> {
            PolicySnapshot snapshot = policyRepo
                    .findByBrokerIdAndExternalProposalId(brokerId, pd.externalProposalId())
                    .orElseGet(PolicySnapshot::new);
            snapshot.setBroker(broker);
            snapshot.setExternalProposalId(pd.externalProposalId());
            snapshot.setInsuredName(pd.insuredName());
            snapshot.setStatus(pd.status());
            snapshot.setStartDate(pd.startDate());
            snapshot.setDueDates(pd.dueDates());
            snapshot.setInsuredCapital(pd.insuredCapital());
            snapshot.setPremiumTotal(pd.premiumTotal());
            snapshot.setInsurer(type.name());
            return snapshot;
        }).toList();

        policyRepo.saveAll(synced);
        return synced.size();
    }

    // Sincronização automática diária às 03:00
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledSync() {
        log.info("[Scheduler] Iniciando sincronização automática de apólices...");
        brokerRepository.findAll().forEach(broker -> {
            try {
                syncAllPolicies(broker.getId());
            } catch (Exception e) {
                log.error("[Scheduler] Erro ao sincronizar broker {}: {}", broker.getId(), e.getMessage());
            }
        });
    }
}
