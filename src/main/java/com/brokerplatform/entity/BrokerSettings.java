package com.brokerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "broker_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrokerSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_id", unique = true, nullable = false)
    private Broker broker;

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "pt-BR";

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "America/Sao_Paulo";

    @Column(name = "notifications_email")
    @Builder.Default
    private boolean notificationsEmail = true;

    @Column(name = "notifications_push")
    @Builder.Default
    private boolean notificationsPush = false;

    @Column(name = "dashboard_theme", length = 20)
    @Builder.Default
    private String dashboardTheme = "light";

    @Column(name = "auto_sync_policies")
    @Builder.Default
    private boolean autoSyncPolicies = true;
}
