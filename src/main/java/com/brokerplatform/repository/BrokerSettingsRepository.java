package com.brokerplatform.repository;

import com.brokerplatform.entity.BrokerSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrokerSettingsRepository extends JpaRepository<BrokerSettings, Long> {

    Optional<BrokerSettings> findByBrokerId(Long brokerId);
}
