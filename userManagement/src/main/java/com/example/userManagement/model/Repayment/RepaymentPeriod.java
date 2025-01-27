package com.example.userManagement.model.Repayment;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum RepaymentPeriod {

    THREE(3),
    SIX(6),
    TWELVE(12);

    private int value;

    public static RepaymentPeriod fromValue(int value) {
        for (RepaymentPeriod period : RepaymentPeriod.values()) {
            if (period.value == value) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid value for repaymentPeriod: " + value);
    }
}
