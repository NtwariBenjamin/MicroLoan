package com.example.userManagement.model.Loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoanWrapper {
    @JsonProperty("loan")
    private Loan loan;
}