package com.benjamin.LoanService.repository;

import com.benjamin.LoanService.model.loan.Loan;
import com.benjamin.LoanService.model.loan.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan,Long> {
    List<Loan> findByMsisdn(String msisdn);


    @Query("SELECT l FROM Loan l WHERE l.msisdn = :msisdn AND l.status = 'APPROVED' ORDER BY l.createdAt DESC")
    Optional<Loan> findLoanByMsisdn(String msisdn);

    @Query("select l.status from Loan l where l.msisdn=:msisdn")
    Optional<LoanStatus> findLoanStatusByMsisdn(@Param("msisdn") String msisdn);
}
