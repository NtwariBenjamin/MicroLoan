package com.example.userManagement.model.response;


import com.example.userManagement.model.Loan.Loan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponse {
    private Loan loan;
    private String message;
}
