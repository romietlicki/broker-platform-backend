package com.brokerplatform.service.insurer;

import java.util.List;

public interface InsurerService {

    InsurerType getType();

    List<PolicyData> fetchPolicies(String brokerCpfCnpj, String clientCpfCnpj);

    PolicyData fetchPolicyDetails(String brokerCpfCnpj, String clientCpfCnpj,
                                  String policyId, String subId);

    List<CommissionData> fetchCommissions(String brokerCpfCnpj, int month, int year);

    boolean isEnabled();
}
