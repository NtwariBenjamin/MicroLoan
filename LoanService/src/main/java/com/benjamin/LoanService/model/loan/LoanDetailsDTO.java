package com.benjamin.LoanService.model.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetailsDTO {
    private String loanAmount;
    private String remainingAmount;
    private String paymentStatus;
}
