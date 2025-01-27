package com.benjamin.LoanService.model.repayment;

import com.benjamin.LoanService.model.loan.Loan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
@Entity
public class Repayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    @Column
    private String paidAmount;

    @Column
    private String remainingAmount;

    @Column
    private LocalDate paymentDate;

    @Column
    @Enumerated(EnumType.STRING)
    private RepaymentStatus paymentStatus;


}
