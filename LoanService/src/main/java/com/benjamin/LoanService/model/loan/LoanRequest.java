package com.benjamin.LoanService.model.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {
    @JsonProperty
    private Long id;


    @JsonProperty
    private String msisdn;

    @JsonProperty
    @NotNull(message = "Amount Cannot be Null")
    private String amount;
    @JsonProperty
    @NotNull(message = "Amount Cannot be Null")
    private String remainingAmount;



    @JsonProperty
    private Integer repaymentPeriod;

    @JsonProperty
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @JsonProperty
    private Timestamp createdAt;

    @JsonProperty
    private Timestamp updatedAt;
}
