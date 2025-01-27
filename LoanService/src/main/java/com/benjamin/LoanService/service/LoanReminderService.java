package com.benjamin.LoanService.service;

import com.benjamin.LoanService.configuration.UserServiceClient;
import com.benjamin.LoanService.model.loan.Loan;
import com.benjamin.LoanService.model.loan.LoanStatus;
import com.benjamin.LoanService.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LoanReminderService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private SmsService smsService;

    @Scheduled(cron = "0 15 12 23 1 ?")
    public void sendPaymentReminders() {

//        LocalDate today = LocalDate.now();
//        LocalDate reminderDate = today.plusDays(30);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime reminderDate = today.plusMinutes(3);

//        String msisdn = null;
//        User userResponse = userServiceClient.getUserDetails(msisdn);
//        log.info("user response: {}", userResponse.getMsisdn());
//
//        Optional<Loan> singleLoan = loanRepository.findLoanByMsisdn(userResponse.getMsisdn());
//        Loan userLoan = singleLoan.get();

        List<Loan> loans = loanRepository.findAll();
        for (Loan loan : loans) {
            if (loan.getStatus().equals(LoanStatus.APPROVED) || loan.getStatus().equals(LoanStatus.APPLICATION)) {
//                LocalDate paymentDate = loan.getCreatedAt().toLocalDateTime().toLocalDate()
//                        .plusMonths(loan.getRepaymentPeriod());
                //                if (paymentDate.isEqual(reminderDate)) {
//                    String message = "Dear customer, your loan repayment of " + loan.getAmount() +
//                            " RWF is due in a week. Please ensure timely payment to avoid penalties.";
//                    smsService.sendSms(loan.getMsisdn(), message);
//                }
//                if (userLoan.getMsisdn().equals(loan.getMsisdn())) {

                    LocalDateTime paymentDate = loan.getCreatedAt().toLocalDateTime().plusMinutes(12);
                    log.info("Loan Repayment Period: {}", loan.getRepaymentPeriod());
                    log.info("Payment Date: {}", paymentDate);
                    log.info("Get Msisdn: {}", loan.getMsisdn());
                    if (paymentDate.isAfter(today) && paymentDate.isBefore(reminderDate)) {
                        String message = "Dear Customer, your loan Repayment of " + loan.getRemainingAmount() +
                                "RWF is due in the next 10 minutes. Please ensure timely payment.";

                        smsService.sendSms(loan.getMsisdn(), message);
                        log.info("Reminder sent to customer with MSISDN: {}", loan.getMsisdn());
                    }
                }
            }
        }
    }

