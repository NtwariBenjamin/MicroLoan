package com.benjamin.LoanService.service;

import com.benjamin.LoanService.exception.LoanNotFoundException;
import com.benjamin.LoanService.exception.UserNotFoundException;
import com.benjamin.LoanService.configuration.UserServiceClient;
import com.benjamin.LoanService.model.loan.Loan;
import com.benjamin.LoanService.model.loan.LoanRequest;
import com.benjamin.LoanService.model.loan.LoanResponse;
import com.benjamin.LoanService.model.loan.LoanStatus;
import com.benjamin.LoanService.model.repayment.Repayment;
import com.benjamin.LoanService.model.repayment.RepaymentStatus;
import com.benjamin.LoanService.model.user.User;
import com.benjamin.LoanService.repository.LoanRepository;
import com.benjamin.LoanService.repository.RepaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LoanApplicationService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RepaymentRepository repaymentRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private SmsService smsService;

public LoanResponse processLoanApplication(String msisdn, LoanRequest loanRequest) {


    User user = userServiceClient.getUserDetails(msisdn);
    if (user == null) {
        throw new UserNotFoundException("User not found with msisdn: " + msisdn);
    }


    Optional<Loan> loanHistoryOptional = loanRepository.findLoanByMsisdn(msisdn);


    if (loanHistoryOptional.isPresent()) {
        Loan existingLoan = loanHistoryOptional.get();
        log.info("Existing loan found: {}", existingLoan);

        if (Double.parseDouble(existingLoan.getRemainingAmount()) == 0.0 && existingLoan.getStatus().equals(LoanStatus.PAID)) {
            List<Repayment> repayments = repaymentRepository.findByLoanId(existingLoan.getId());
            boolean isFullyPaid = repayments.stream()
                    .allMatch(repayment -> repayment.getPaymentStatus() == RepaymentStatus.PAID);

            if (isFullyPaid) {
                log.info("Previous loan is fully paid. Creating a new loan for MSISDN: {}", msisdn);
            } else {
                String message="Dear Customer,You already have an active loan that is not fully paid.";
                smsService.sendSms(existingLoan.getMsisdn(),message);
                return LoanResponse.builder()
                        .loan(existingLoan)
                        .message("You already have an active loan that is not fully paid.")
                        .build();
            }
        } else {
                String message="Dear Customer,You already have an active loan with outstanding payments.";
                smsService.sendSms(existingLoan.getMsisdn(),message);
            return LoanResponse.builder()
                    .loan(existingLoan)
                    .message("You already have an active loan with outstanding payments.")
                    .build();
        }
    }

    Double loanInterest = Double.parseDouble(loanRequest.getAmount()) * 0.10;
    Double totalAmount = Double.parseDouble(loanRequest.getAmount()) + loanInterest;

    log.info("Verify loan Interest: {}",loanInterest);
    LoanStatus loanStatus = initialLoanStatus(user, loanHistoryOptional.stream().toList());


    Loan newLoan = Loan.builder()
            .amount(String.valueOf(totalAmount))
            .repaymentPeriod(loanRequest.getRepaymentPeriod())
            .msisdn(msisdn)
            .remainingAmount(String.valueOf(totalAmount))
            .createdAt(Timestamp.valueOf(LocalDateTime.now()))
            .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
            .status(loanStatus)
            .build();
    log.info("Verify loan Interest: {}",loanInterest);
    Loan savedLoan = loanRepository.save(newLoan);
    log.info("Created Loan: {}", savedLoan);

    Repayment newRepayment = Repayment.builder()
            .loan(savedLoan)
            .remainingAmount(loanRequest.getRemainingAmount())
            .paidAmount("0")
            .paymentDate(LocalDate.now().plusMonths(loanRequest.getRepaymentPeriod()))
            .paymentStatus(RepaymentStatus.PENDING)
            .build();
    repaymentRepository.save(newRepayment);

    log.info("New loan and repayment record created for MSISDN: {}", msisdn);

    if (savedLoan != null && savedLoan.getMsisdn() != null) {
        String message = "Dear customer, your loan of " + savedLoan.getAmount() +
                " RWF has been approved. Repayment is due by " +
                savedLoan.getRepaymentPeriod() + " months. Thank you!";
        smsService.sendSms(msisdn, message);
        log.info("SMS for Loan Approval: {}",message);
    }
    return LoanResponse.builder()
            .loan(newLoan)
            .message("Loan Requested Successfully!")
            .build();
}



    public LoanResponse updateLoanApplication(String msisdn, Loan loanRequest) {

        User user = userServiceClient.getUserDetails(msisdn);
        if (user == null) {
            throw new UserNotFoundException("User not found with msisdn: " + msisdn);
        }


        List<Loan> loanHistory = loanRepository.findByMsisdn(msisdn);
        Loan existingLoan = null;


        if (loanHistory != null && !loanHistory.isEmpty()) {
            existingLoan = loanHistory.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.APPLICATION || loan.getStatus() == LoanStatus.APPROVED ||
                            Double.parseDouble(loan.getRemainingAmount()) > 0)
                    .findFirst()
                    .orElse(null);
        }

        if (existingLoan != null  ) {

            existingLoan.setAmount(loanRequest.getAmount());
            existingLoan.setRepaymentPeriod(loanRequest.getRepaymentPeriod());
            existingLoan.setRemainingAmount(loanRequest.getAmount());
            existingLoan.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

            Loan updatedLoan = loanRepository.save(existingLoan);


            List<Repayment> repayments = repaymentRepository.findByLoanId(existingLoan.getId());
            Repayment repayment=repayments.get(0);

            if (repayment != null) {
                repayment.setRemainingAmount(loanRequest.getAmount()+repayment.getRemainingAmount());
                repayment.setPaidAmount(repayment.getPaidAmount());
                repayment.setPaymentDate(LocalDate.now().plusMonths(loanRequest.getRepaymentPeriod()));
                repayment.setPaymentStatus(RepaymentStatus.PENDING);
                repaymentRepository.save(repayment);
            } else {
                repayment = Repayment.builder()
                        .loan(updatedLoan)
                        .remainingAmount(loanRequest.getAmount())
                        .paidAmount("0")
                        .paymentDate(LocalDate.now().plusMonths(loanRequest.getRepaymentPeriod()))
                        .paymentStatus(RepaymentStatus.PENDING)
                        .build();
                repaymentRepository.save(repayment);
            }

            return LoanResponse.builder()
                    .loan(updatedLoan)
                    .message("Loan Updated Successfully!")
                    .build();
        } else {

            LoanStatus loanStatus = initialLoanStatus(user, loanHistory);


            Loan newLoan = Loan.builder()
                    .amount(loanRequest.getAmount())
                    .repaymentPeriod(loanRequest.getRepaymentPeriod())
                    .msisdn(msisdn)
                    .remainingAmount(loanRequest.getAmount())
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .status(loanStatus)
                    .build();

            Loan savedLoan = loanRepository.save(newLoan);


            Repayment repayment = Repayment.builder()
                    .loan(savedLoan)
                    .remainingAmount(loanRequest.getAmount())
                    .paidAmount("0")
                    .paymentDate(LocalDate.now().plusMonths(loanRequest.getRepaymentPeriod()))
                    .paymentStatus(RepaymentStatus.PENDING)
                    .build();
            repaymentRepository.save(repayment);

            return LoanResponse.builder()
                    .loan(savedLoan)
                    .message("Loan Requested Successfully!")
                    .build();
        }
    }

    private LoanStatus initialLoanStatus(User user, List<Loan> loanHistory) {

        if (loanHistory == null || loanHistory.isEmpty()) {

            return user.getLoanEligibility() ? LoanStatus.APPROVED : LoanStatus.REJECTED;
        }


        boolean hasGoodRepaymentHistory = loanHistory.stream()
                .anyMatch(loan -> loan.getStatus() == LoanStatus.PAID || loan.getStatus() == LoanStatus.APPROVED);

        if (hasGoodRepaymentHistory || user.getLoanEligibility()) {
            return LoanStatus.APPROVED;
        }

        return LoanStatus.REJECTED;
    }


    public LoanResponse updateLoan(Long loanId,LoanRequest loanRequest) throws LoanNotFoundException {

       Optional<Loan> loanOptional=loanRepository.findById(loanId);

        if (loanOptional.isEmpty()) {
            throw new LoanNotFoundException("Loan Not Found");
        }
        Loan existingLoan = loanOptional.get();

        if (loanRequest.getRemainingAmount() == null || loanRequest.getStatus() == null) {
            throw new IllegalArgumentException("Invalid loan request data.");
        }

        existingLoan.setRemainingAmount(loanRequest.getRemainingAmount());
        existingLoan.setStatus(loanRequest.getStatus());
        existingLoan.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        Loan updatedLoan = loanRepository.save(existingLoan);
        log.info("Loan updated in the database: {}", updatedLoan);
        if (updatedLoan != null && updatedLoan.getMsisdn() != null) {
            Double paidAmount=Double.parseDouble(updatedLoan.getAmount())-Double.parseDouble(updatedLoan.getRemainingAmount());
            String message = "Dear customer, Your total Paid Amount is " +String.valueOf(paidAmount)+
                    " RWF, The Remaining Amount is"+existingLoan.getRemainingAmount()+"RWF. Thank you!";
            smsService.sendSms(updatedLoan.getMsisdn(), message);
            log.info("SMS for Loan Repayment: {}",message);

        }
        if (Double.parseDouble(updatedLoan.getRemainingAmount())==0){
            String text="Dear Customer,Congratulations You have fully paid your Loan!";
            smsService.sendSms(updatedLoan.getMsisdn(),text);
        }

        log.info("This loan: {}",updatedLoan);



        return LoanResponse.builder()
                .loan(updatedLoan)
                .message("Loan Updated Successfully!")
                .build();
    }

    public List<LoanResponse> getAllLoans() {
        log.info("Fetching all loans from database");
        List<Loan> loans = loanRepository.findAll();
        return loans.stream()
                .map(loan -> LoanResponse.builder()
                        .loan(loan)
                        .message("List of All Loans")
                        .build())
                .collect(Collectors.toList());
    }

    public LoanResponse getLoanDetails(String msisdn) {
        List<Loan> loan = loanRepository.findByMsisdn(msisdn);

        return LoanResponse.builder()
                .loan((Loan) loan)
                .message("Details of Loan with Msisdn: " + loan)
                .build();
    }

    public String mapAdminChoiceToStatus(int choice) {
        if (choice == 1) {
            return "APPROVED";
        } else if (choice == 2) {
            return "REJECTED";
        } else {
            throw new IllegalArgumentException("Invalid choice: " + choice + ". Allowed choices: 1 for APPROVED, 2 for REJECTED.");
        }
    }

    public Optional<LoanStatus> getLoanByMsisdn(String msisdn) {
        if (msisdn == null || msisdn.isBlank()) {
            throw new IllegalArgumentException("MSISDN cannot be null or blank");
        }

        Optional<LoanStatus> loan = loanRepository.findLoanStatusByMsisdn(msisdn);

        if (loan.isEmpty()) {
            log.info("No loan found for MSISDN: {}", msisdn);
            return Optional.empty();
        }


        return loan;
    }
    public Loan getLoanForRepaymentByMsisdn(String msisdn) {

        if (msisdn == null || msisdn.isBlank()) {
            throw new IllegalArgumentException("MSISDN cannot be null or blank");
        }
        Optional<Loan> loan= loanRepository.findLoanByMsisdn(msisdn);

        log.info("loan by MSISDN: {}",msisdn);
        log.info("loan: {}",loan);
        if (loan.isEmpty()) {
            throw new RuntimeException("Loan not found for MSISDN: " + msisdn);
        }
        log.info("Loan: {}", loan.get());
        return loan.get();
    }

    public List<Repayment> getLoanDetailss(String msisdn) {
        Optional<Loan> loan = loanRepository.findLoanByMsisdn(msisdn);

        if (loan.isEmpty()) {
            log.info("No loan found for MSISDN: {}", msisdn);
            return Collections.emptyList();
        }

        Long loanId = loan.get().getId();
        List<Repayment> repayments = repaymentRepository.findByLoanId(loanId);


        if (repayments.isEmpty()) {
            log.info("No repayment records found for Loan ID: {}", loanId);
        }

        return repayments;
    }

    public Repayment getRepayment(Long loanId) {
        return repaymentRepository.findTopByLoanIdOrderByPaymentDateDesc(loanId);
    }
        public Repayment updateRepayment(Repayment repayment) throws Exception {
            Optional<Repayment> existingRepaymentOpt = repaymentRepository.findById(repayment.getId());
            if (existingRepaymentOpt.isEmpty()) {
                throw new Exception("Repayment record  not found.");
            }

            Repayment existingRepayment = existingRepaymentOpt.get();

            existingRepayment.setPaidAmount(repayment.getPaidAmount());
            existingRepayment.setRemainingAmount(repayment.getRemainingAmount());
            existingRepayment.setPaymentDate(LocalDate.now());
            if (Double.parseDouble(existingRepayment.getRemainingAmount())==0){
                existingRepayment.setPaymentStatus(RepaymentStatus.PAID);
            }

            return repaymentRepository.save(existingRepayment);
        }

    public LoanResponse getRepaymentByMsisdn(String msisdn) {
        if (msisdn == null || msisdn.isBlank()) {
            throw new IllegalArgumentException("MSISDN cannot be null or blank.");
        }

        User user = userServiceClient.getUserDetails(msisdn);
        if (user == null) {
            throw new UserNotFoundException("User not found with MSISDN: " + msisdn);
        }
        log.info("Fetching loan details for MSISDN: {}", msisdn);

        Optional<Loan> loanOptional = loanRepository.findLoanByMsisdn(msisdn);
        if (loanOptional.isEmpty()) {
            log.info("No existing loan found for MSISDN: {}. Creating a new loan.", msisdn);
            return LoanResponse.builder().loan(null).message("No existing loan found for MSISDN: "+msisdn).build();
        }


        Loan loan = loanOptional.get();
        log.info("Existing loan found: {}", loan);

        return LoanResponse.builder().loan(loan).message("Loan Is found").build();
    }

}


