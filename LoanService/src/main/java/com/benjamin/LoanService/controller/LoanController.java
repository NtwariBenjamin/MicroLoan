package com.benjamin.LoanService.controller;

import com.benjamin.LoanService.configuration.JwtService;
import com.benjamin.LoanService.configuration.UserServiceClient;
import com.benjamin.LoanService.exception.LoanNotFoundException;
import com.benjamin.LoanService.model.loan.*;
import com.benjamin.LoanService.model.repayment.Repayment;
import com.benjamin.LoanService.model.repayment.RepaymentStatus;
import com.benjamin.LoanService.model.user.User;
import com.benjamin.LoanService.repository.RepaymentRepository;
import com.benjamin.LoanService.service.LoanApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/loan")
@Slf4j
public class LoanController {
    @Autowired
    private LoanApplicationService loanService;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private RepaymentRepository repaymentRepository;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> applyForLoan(@RequestBody LoanRequest loanRequest,
                                               @RequestHeader("Authorization") String authToken) {
        LoanResponse loanResponse;

        String msisdn = loanRequest.getMsisdn();
        log.info("Extracted msisdn: {}", msisdn);
        User userResponse = userServiceClient.getUserDetails(msisdn);
        log.info("Getting user {}",userResponse);
        if (userResponse == null || userResponse.getMsisdn() == null) {
            log.warn("User not found for msisdn: {}", msisdn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(LoanResponse.builder().message("User not found").build());
        }
        if (!Boolean.TRUE.equals(userResponse.getLoanEligibility())) {
            log.warn("User is not eligible for a loan: {}", msisdn);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoanResponse.builder().message("User is not eligible for a loan").build());
        }
        log.info("Creating a new Loan Application with msisdn: {}",msisdn);
        loanResponse=loanService.processLoanApplication(msisdn,loanRequest);
        return ResponseEntity.ok(loanResponse);
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateLoan(@RequestBody Loan loanRequest,
                                                     @RequestHeader("Authorization") String authToken) {
        LoanResponse loanResponse;

        String msisdn = loanRequest.getMsisdn();
        log.info("Extracted msisdn: {}", msisdn);
        User userResponse = userServiceClient.getUserDetails(msisdn);
        log.info("Getting user {}",userResponse);
        if (userResponse == null || userResponse.getMsisdn() == null) {
            log.warn("User not found for msisdn: {}", msisdn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(LoanResponse.builder().message("User not found").build());
        }
        if (!Boolean.TRUE.equals(userResponse.getLoanEligibility())) {
            log.warn("User is not eligible for a loan: {}", msisdn);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoanResponse.builder().message("User is not eligible for a loan").build());
        }
        log.info("Creating a new Loan Application with msisdn: {}",msisdn);
        loanResponse=loanService.updateLoanApplication(msisdn,loanRequest);
        return ResponseEntity.ok(loanResponse);
    }

    @GetMapping("/repayment/latest/{loanId}")
    public ResponseEntity<?> getRepaymentByLoanId(@PathVariable Long loanId){
        try{
            log.info("Getting Repayment By Loan Id: {}",loanId);
            Repayment repayment=loanService.getRepayment(loanId);
            if (repayment == null) {
                log.warn("No repayment found for Loan ID: {}", loanId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No repayment record found for Loan ID: " + loanId);
            }
             return ResponseEntity.ok(repayment);
        }catch (Exception e) {
            log.error("Error fetching repayment for Loan ID: {}", loanId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching repayment. Please try again later.");
        }
    }

    @PutMapping("/repayment/update")
    public ResponseEntity<?> updateRepayment(@RequestBody Repayment repayment){
        try {
            log.info("Updating Repayment Record: {}",repayment);
            Repayment repayment1=loanService.updateRepayment(repayment);
            return ResponseEntity.ok(repayment1);
        }catch (Exception e) {
            log.error("Error updating repayment record for ID: {}", repayment.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update repayment record. Please try again later.");
        }
    }

    @PutMapping("update/{loanId}")
    public  ResponseEntity<LoanResponse> updateLoanStatus(@PathVariable Long loanId,@RequestBody LoanRequest loanRequest){
        LoanResponse loanResponse;
        try {
            log.info("Updating loan for MSISDN: {}",loanId);
            loanResponse=loanService.updateLoan(loanId,loanRequest);
            log.info("Loan Response {}",loanResponse);
            return ResponseEntity.ok(loanResponse);
        }catch (Exception e) {
            log.warn("Failed to update loan For Msisdn {} ",loanId);
            throw new IllegalArgumentException("An error occurred while updating Loan");
        }
    }

    @GetMapping("/loans")
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        try {
            log.info("Fetching all loans for admin");
            List<LoanResponse> loans = loanService.getAllLoans();
            return ResponseEntity.ok(loans);
        }catch (Exception e){
            log.error("Failed to Retrieve Loans");
           throw new IllegalArgumentException("Error Retrieving Loans");
        }
    }

    @GetMapping("/loans/{msisdn}")
    public ResponseEntity<LoanResponse> getLoanDetails(@PathVariable String msisdn) {
        try {
            log.info("Fetching details for loan ID: {}", msisdn);
            LoanResponse loanDetails = loanService.getLoanDetails(msisdn);
            return ResponseEntity.ok(loanDetails);
        }catch (Exception e){
            throw new IllegalArgumentException("Failed To Retrieve Loan Details");
        }
    }

    @GetMapping("/{msisdn}")
    public ResponseEntity<?> getLoanByMsisdn(@PathVariable String msisdn) {
        try {
            log.info("Retrieving Loan by msisdn: {}", msisdn);
            List<Repayment> loans=loanService.getLoanDetailss(msisdn);
            log.info("Checking out Loan Details: {}",loans);
            Optional<LoanStatus> loanResponse = loanService.getLoanByMsisdn(msisdn);

            if(loans.isEmpty()){
                log.info("No Active Loans for Msisdn: {}",msisdn);
                return ResponseEntity.ok("No Active Loans Found For Your Account");
            }
            return ResponseEntity.ok(loans);
        } catch (IllegalArgumentException e) {
            log.error("Invalid MSISDN provided: {}",msisdn  , e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid MSISDN: " + msisdn);
        } catch (Exception e) {
            log.error("Error retrieving loan by MSISDN: {}", msisdn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving loan details. Please try again later.");
        }
    }
    @GetMapping("/repayment/{msisdn}")
    public ResponseEntity<?> getLoanForRepaymentByMsisdn(@PathVariable String msisdn) {
        try {
            log.info("Retrieving Loan by msisdn: {}", msisdn);
            LoanResponse repaymentResponse = loanService.getRepaymentByMsisdn(msisdn);
            log.info("Check Loan Response: {}",repaymentResponse);

            return ResponseEntity.ok(repaymentResponse);
        } catch (IllegalArgumentException e) {
            log.error("Invalid MSISDN provided: {}", msisdn, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid MSISDN format. Please check and try again.");
        } catch (LoanNotFoundException e) {
            log.error("Loan not found for MSISDN: {}", msisdn, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active loan found for the provided MSISDN.");
        } catch (Exception e) {
            log.error("Unexpected error while retrieving loan for MSISDN: {}", msisdn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }


    @PostMapping("/repay")
    public ResponseEntity<Repayment> repayLoan(@RequestBody Repayment repaymentRequest) {
        Loan loan = loanService.getLoanForRepaymentByMsisdn(repaymentRequest.getLoan().getMsisdn());
        if (loan == null) {
            throw new RuntimeException("Loan not found for MSISDN: " + repaymentRequest.getLoan().getMsisdn());
        }
        if (repaymentRequest.getLoan() == null || repaymentRequest.getLoan().getId() == null) {
            throw new IllegalArgumentException("Loan ID is required for repayment.");
        }



        try {
            String loanAmountStr = repaymentRequest.getLoan().getAmount();
            String paidAmountStr = repaymentRequest.getPaidAmount();

            if (loanAmountStr == null || paidAmountStr == null) {
                return ResponseEntity.badRequest().body(null);
            }

            Double loanAmount = Double.parseDouble(loanAmountStr);
            Double paidAmount = Double.parseDouble(paidAmountStr);

            if (loanAmount <= 0 || paidAmount <= 0) {
                return ResponseEntity.badRequest().body(null);
            }

            // Calculate remaining amount
            Double remainingAmount = loanAmount - paidAmount;

            // Create repayment response
            Repayment response = new Repayment();
            response.setPaidAmount(paidAmountStr);
            response.setLoan(loan);

            // Determine repayment status
            if (remainingAmount > 0) {
                response.setPaymentStatus(RepaymentStatus.HALF_PAID);
                response.setRemainingAmount(String.valueOf(remainingAmount));
            } else if (remainingAmount == 0) {
                response.setPaymentStatus(RepaymentStatus.PAID);
                response.setRemainingAmount("0.0");
            } else {
                response.setPaymentStatus(RepaymentStatus.OVERDUE);
                response.setRemainingAmount("0.0");
            }

            repaymentRepository.save(response);
            // Return successful response
            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            // Handle invalid number format in amount
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    private String extractMsisdnFromToken(String authToken) {
        // Extract and return msisdn from JWT token
        String token = authToken.substring(7);
        return jwtService.extractUsername(token);
    }
}
