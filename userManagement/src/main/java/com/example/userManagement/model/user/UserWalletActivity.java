package com.example.userManagement.model.user;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "user_wallet_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWalletActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="msisdn",nullable = false)
    private String msisdn;

    @Column(name = "total_amount_sent", nullable = false)
    private Double totalAmountSent = 0.0;

    @Column(name = "total_amount_received", nullable = false)
    private Double totalAmountReceived = 0.0;

    @Column(name = "total_recharge_amount", nullable = false)
    private Double totalRechargeAmount = 0.0;

    @Column(name = "recharge_frequency", nullable = false)
    private Integer rechargeFrequency = 0;

    @Column(name = "last_recharge_date")
    private Timestamp lastRechargeDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
