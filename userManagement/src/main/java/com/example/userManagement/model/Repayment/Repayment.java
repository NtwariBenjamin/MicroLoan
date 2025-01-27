package com.example.userManagement.model.Repayment;

import com.example.userManagement.model.Loan.Loan;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Repayment {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Loan loan;

    @Column
    @NotNull(message = "Paid amount cannot be null")
    @Positive(message = "Paid amount must be positive")
    @JsonProperty
    private String paidAmount;

    @Column
    @JsonProperty
    private String remainingAmount;

    @Column
    @JsonProperty
    private LocalDate paymentDate;

    @Column
    @Enumerated(EnumType.STRING)
    @JsonProperty
    private RepaymentStatus paymentStatus;


}
