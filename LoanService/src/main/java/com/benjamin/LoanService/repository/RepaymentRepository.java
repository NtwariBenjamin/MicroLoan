package com.benjamin.LoanService.repository;

import com.benjamin.LoanService.model.repayment.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment,Long> {


    List<Repayment> findByLoanId(Long loanId);


    Repayment findRepaymentByLoanId(Long loanId);
    @Query("SELECT r FROM Repayment r WHERE r.loan.id = :loanId ORDER BY r.paymentDate DESC, r.id DESC")
    Repayment findTopByLoanIdOrderByPaymentDateDesc(@Param("loanId") Long loanId);
}
