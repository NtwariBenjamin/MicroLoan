package com.example.userManagement.model.Repayment;

import com.example.userManagement.model.Loan.Loan;
import lombok.Data;

@Data
public class RepaymentWrapper {
    private Long id;
    private Loan loan;
    private String paidAmount;
    private String remainingAmount;
    private String paymentDate;
    private String paymentStatus;
}