package com.example.userManagement.model.Loan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Loan {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String msisdn;

    @JsonProperty
    private String amount;


    @JsonProperty
    private String remainingAmount;


    private Integer repaymentPeriod;

    @JsonProperty

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @JsonProperty
    private Timestamp createdAt;

    @JsonProperty
    private Timestamp updatedAt;

}
