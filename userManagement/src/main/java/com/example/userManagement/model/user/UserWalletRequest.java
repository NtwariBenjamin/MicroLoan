package com.example.userManagement.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWalletRequest {
    @JsonProperty
    private String msisdn;
    @JsonProperty
    private Double totalAmountSent = 0.0;

    @JsonProperty
    private Double totalAmountReceived = 0.0;

    @JsonProperty
    private Double totalRechargeAmount = 0.0;

    @JsonProperty
    private Integer rechargeFrequency = 0;

    @JsonProperty
    private Timestamp lastRechargeDate;

    @JsonProperty
    @Builder.Default
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @JsonProperty
    @Builder.Default
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
