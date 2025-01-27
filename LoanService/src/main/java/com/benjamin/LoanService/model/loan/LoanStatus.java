package com.benjamin.LoanService.model.loan;

import lombok.Builder;
import lombok.Data;


public enum LoanStatus {
    APPROVED,
    APPLICATION,
    REJECTED,
    PAID
}
