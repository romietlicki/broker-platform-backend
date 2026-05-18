package com.brokerplatform.service;

import com.brokerplatform.dto.request.UpdateBrokerRequest;
import com.brokerplatform.dto.response.BrokerResponse;
import com.brokerplatform.entity.Broker;
import com.brokerplatform.exception.ResourceNotFoundException;
import com.brokerplatform.repository.BrokerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrokerService {

    private final BrokerRepository brokerRepository;

    @Transactional(readOnly = true)
    public BrokerResponse getById(Long id) {
        Broker broker = brokerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corretor", id));
        return BrokerResponse.from(broker);
    }

    @Transactional
    public BrokerResponse update(Long id, UpdateBrokerRequest request) {
        Broker broker = brokerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corretor", id));

        if (request.fullName() != null) broker.setFullName(request.fullName());
        if (request.phone() != null) broker.setPhone(request.phone());
        if (request.addressStreet() != null) broker.setAddressStreet(request.addressStreet());
        if (request.addressCity() != null) broker.setAddressCity(request.addressCity());
        if (request.addressState() != null) broker.setAddressState(request.addressState());
        if (request.addressZip() != null) broker.setAddressZip(request.addressZip());

        return BrokerResponse.from(brokerRepository.save(broker));
    }
}
