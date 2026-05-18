package com.brokerplatform.service;

import com.brokerplatform.dto.response.CommissionResponse;
import com.brokerplatform.entity.Broker;
import com.brokerplatform.exception.ResourceNotFoundException;
import com.brokerplatform.repository.BrokerRepository;
import com.brokerplatform.service.insurer.CommissionData;
import com.brokerplatform.service.insurer.InsurerServiceFactory;
import com.brokerplatform.service.insurer.InsurerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionService {

    private final InsurerServiceFactory insurerFactory;
    private final BrokerRepository brokerRepository;

    public List<CommissionResponse> getCommissions(Long brokerId, Integer month, Integer year) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new ResourceNotFoundException("Corretor", brokerId));

        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        List<CommissionResponse> all = new ArrayList<>();

        insurerFactory.getAll().forEach(svc -> {
            try {
                List<CommissionData> data = svc.fetchCommissions(broker.getCpf(), targetMonth, targetYear);
                data.stream()
                        .map(cd -> new CommissionResponse(
                                cd.externalProposalId(),
                                cd.insuredName(),
                                svc.getType().name(),
                                cd.productName(),
                                cd.commissionAmount(),
                                cd.commissionPercentage(),
                                cd.referenceMonth(),
                                cd.status(),
                                cd.paymentDate()
                        ))
                        .forEach(all::add);
            } catch (Exception e) {
                log.error("[Commission] Erro ao buscar comissionamento de {} para broker {}: {}",
                        svc.getType(), brokerId, e.getMessage());
            }
        });

        return all;
    }

    public List<CommissionResponse> getCommissionsByInsurer(Long brokerId, InsurerType type,
                                                            int month, int year) {
        Broker broker = brokerRepository.findById(brokerId)
                .orElseThrow(() -> new ResourceNotFoundException("Corretor", brokerId));

        return insurerFactory.get(type)
                .fetchCommissions(broker.getCpf(), month, year)
                .stream()
                .map(cd -> new CommissionResponse(
                        cd.externalProposalId(),
                        cd.insuredName(),
                        type.name(),
                        cd.productName(),
                        cd.commissionAmount(),
                        cd.commissionPercentage(),
                        cd.referenceMonth(),
                        cd.status(),
                        cd.paymentDate()
                ))
                .toList();
    }
}
