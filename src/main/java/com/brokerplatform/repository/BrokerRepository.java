package com.brokerplatform.repository;

import com.brokerplatform.entity.Broker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrokerRepository extends JpaRepository<Broker, Long> {

    Optional<Broker> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsBySusepCode(String susepCode);

    @Query("SELECT b FROM Broker b LEFT JOIN FETCH b.roles WHERE b.email = :email")
    Optional<Broker> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT b FROM Broker b LEFT JOIN FETCH b.roles WHERE b.passwordResetToken = :token")
    Optional<Broker> findByPasswordResetToken(@Param("token") String token);
}
