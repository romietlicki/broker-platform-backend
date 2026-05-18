package com.brokerplatform.service.insurer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PolicyData(
        String externalProposalId,
        String insuredName,
        String insuredCpf,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        List<String> dueDates,
        String coverageDescription,
        BigDecimal insuredCapital,
        BigDecimal premiumTotal,
        String paymentPeriodicity,
        String productName,
        Integer gracePeriodDays,
        LocalDate cancellationDate
) {}
