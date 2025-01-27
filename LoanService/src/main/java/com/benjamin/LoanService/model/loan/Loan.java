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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "loan")
@Entity
public class Loan {
    @Id
    @Column(name = "loanId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "msisdn", nullable = false)
    @JsonProperty
    private String msisdn;

    @JsonProperty
    @NotNull(message = "Amount Cannot be Null")
    @Column
    private String amount;

    @Column
    @JsonProperty
    private String remainingAmount;

    @Column
    @JsonProperty
    private Integer repaymentPeriod;

    @JsonProperty
    @NotNull(message = "Status cannot be null")
//    @Pattern(regexp = "APPLICATION|REJECTED|REPAID|APPROVED", message = "Invalid status value")
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @JsonProperty
    private Timestamp createdAt;

    @JsonProperty
    private Timestamp updatedAt;

}
