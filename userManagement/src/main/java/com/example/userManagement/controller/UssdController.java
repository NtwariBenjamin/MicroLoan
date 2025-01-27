package com.example.userManagement.controller;

import com.example.userManagement.model.Loan.Loan;
import com.example.userManagement.model.Loan.LoanRequest;
import com.example.userManagement.model.Loan.LoanStatus;
import com.example.userManagement.model.Repayment.Repayment;
import com.example.userManagement.model.Repayment.RepaymentStatus;
import com.example.userManagement.model.response.LoanResponse;
import com.example.userManagement.model.response.LoginResponse;
import com.example.userManagement.model.user.LoginRequest;
import com.example.userManagement.model.user.Role;
import com.example.userManagement.model.user.UserRequest;
import com.example.userManagement.model.user.UserWalletActivity;
import com.example.userManagement.repository.UserRepository;
import com.example.userManagement.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/ussd")
@Slf4j
public class UssdController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserController userController;

    @Autowired
    private LoanService loanService;
    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserWalletService userWalletService;

    @PostMapping
    public String handleUssdRequest(@RequestParam String sessionId,
                                    @RequestParam String phoneNumber,
                                    @RequestParam String text) {

        String[] inputs = text.split("\\*");
        String msisdn = phoneNumber;


        if (text.isEmpty()) {
            return displayWelcomeMenu(sessionId);
        }

        String menuLevel = sessionService.getMenuLevel(sessionId);
        log.info("Session ID: {}", sessionId);
        log.info("Phone Number: {}", phoneNumber);
        log.info("Text: {}", text);

        try {
            switch (menuLevel) {
                case "LOGIN_OR_REGISTER":
                    return handleLoginOrRegister(sessionId, msisdn, inputs);

                case "REGISTRATION":
                    return handleRegistration(sessionId, msisdn, inputs);

                case "LOGIN":
                    return handleLogin(sessionId, msisdn, inputs);

                case "MAIN_MENU":
                    return handleMainMenuSelection(sessionId, msisdn, inputs);

                case "REPAYMENT_MENU":
                    return handleRepayment(sessionId, msisdn, inputs);

                case "LOAN_APPLICATION":
                    return handleLoanApplication(sessionId, msisdn, inputs);

                case "LOAN_STATUS":
                    return handleLoanStatus(sessionId, msisdn);

                default:
                    return displayWelcomeMenu(sessionId);
            }
        } catch (Exception e) {
            log.error("Error handling USSD request", e);
            return "END An unexpected error occurred. Please try again later.";
        }
    }

    private String displayWelcomeMenu(String sessionId) {
        sessionService.setMenuLevel(sessionId, "LOGIN_OR_REGISTER");
        return "CON Welcome to the Loan Service. Choose an option:\n" +
                "1. Register\n" +
                "2. Login";
    }

    private String handleLoginOrRegister(String sessionId, String msisdn, String[] inputs) {
        if ("1".equals(inputs[0])) {
            return handleRegistration(sessionId, msisdn, inputs);
        } else if ("2".equals(inputs[0])) {
            return handleLogin(sessionId, msisdn, inputs);
        } else {
            return "END Invalid input.";
        }
    }

    private String handleRegistration(String sessionId, String msisdn, String[] inputs) {

        HashMap<String, String> payload = (HashMap<String, String>) redisTemplate.opsForValue().get(sessionId);
        if (payload == null) {
            payload = new HashMap<>();
            payload.put("step", "name");
            redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));
            return "CON Enter your Name:";
        }

        String step = payload.get("step");


        switch (step) {
            case "name":
                if (inputs.length > 1) {
                    payload.put("name", inputs[inputs.length - 1]);
                    payload.put("step", "id");
                    redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));
                    return "CON Enter your ID Number:";
                } else {
                    return "CON Name cannot be empty. Please enter your Name:";
                }

            case "id":
                if (inputs.length > 2) {
                    payload.put("id", inputs[inputs.length - 1]);
                    payload.put("step", "location");
                    redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));
                    return "CON Enter your Location:";
                } else {
                    return "CON ID cannot be empty. Please enter your ID:";
                }

            case "location":
                if (inputs.length > 3) {
                    payload.put("location", inputs[inputs.length - 1]);
                    payload.put("step", "password");
                    redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));
                    return "CON Create a Password:";
                } else {
                    return "CON Location cannot be empty. Please enter your Location:";
                }

            case "password":
                if (inputs.length > 4) {
                    payload.put("password", inputs[inputs.length - 1]);


                    UserRequest userRequest = UserRequest.builder()
                            .name(payload.get("name"))
                            .id(payload.get("id"))
                            .location(payload.get("location"))
                            .msisdn(msisdn)
                            .password(payload.get("password"))
                            .loanEligibility(true)
                            .role(Role.BORROWER)
                            .build();

                    log.info("Saving User {}", userRequest);
                    userService.registerUser(userRequest);

                    String message="Dear "+userRequest.getName()
                            +", Your Account Has been Created Successfully! Please dial *384*36344# to Log in.";
                    smsService.sendSms(msisdn,message);
                    redisTemplate.delete(sessionId);

                    return "END Registration successful! Please dial *384*36344# to log in.";
                } else {
                    return "CON Password cannot be empty. Please create a Password:";
                }

            default:
                return "END Invalid input.";
        }
    }


    private String handleLogin(String sessionId, String msisdn, String[] inputs) {
        if (inputs.length == 1) {
            sessionService.setMenuLevel(sessionId, "LOGIN");
            return "CON Enter your Password:";
        }
        LoginRequest loginRequest = LoginRequest.builder()
                .msisdn(msisdn)
                .password(inputs[1])
                .build();
        try {
            LoginResponse loginResponse = userController.login(loginRequest).getBody();
            if (loginResponse != null && loginResponse.getToken() != null) {

                sessionService.setMenuLevel(sessionId, "MAIN_MENU");
                return "CON Welcome to the Main Menu:\n" +
                        "1. Request Loan\n" +
                        "2. Repay Loan\n" +
                        "3. Check Loan Status\n" +
                        "4. Exit";
            } else {
                return "END Login failed. Invalid credentials.";
            }
        } catch (Exception e) {
            return "END Invalid credentials. Please try again.";
        }
    }

    private String handleMainMenuSelection(String sessionId, String msisdn, String[] inputs) {
        log.info("User selected option: {}", inputs[inputs.length - 1]);

        HashMap<String, String> payload = (HashMap<String, String>) redisTemplate.opsForValue().get(sessionId);
        if (payload != null && payload.get("step") != null) {
            log.info("Continue existing session:{}", payload);
            return handleLoanApplication(sessionId, msisdn, inputs);
        }

        if (inputs.length == 0 || inputs[inputs.length - 1].isBlank()) {
            return "END Invalid input. Please try again.";
        }
        log.info("Handling main menu selection for session {}: option {}", sessionId, inputs[0]);

        switch (inputs[inputs.length - 1].trim()) {
            case "1":
                return handleLoanApplication(sessionId, msisdn, inputs);

            case "2":
                sessionService.setMenuLevel(sessionId, "REPAYMENT_MENU");
                log.info("Entering repayment menu for MSISDN: {}", msisdn);
                return handleRepayment(sessionId, msisdn, inputs);

            case "3":
                return handleLoanStatus(sessionId, msisdn);

            case "4":
                log.info("EXIT");
                return "END Thank you for using our service!";

            default:
                return "END Invalid input.";
        }
    }
    private String handleLoanApplication(String sessionId, String msisdn, String[] inputs) {


        HashMap<String, String> payload = (HashMap<String, String>) redisTemplate.opsForValue().get(sessionId);
        log.info("Show PAYLOAD: {}", payload);
        UserWalletActivity userWallet= userWalletService.getUserWalletByMsisdn(msisdn).getUserWalletActivity();
        Double receivedAmount=userWallet.getTotalAmountReceived();
        Double sentAmount=userWallet.getTotalAmountSent();
        Double rechargedAmount=userWallet.getTotalRechargeAmount();
        Integer rechargeFrequency=userWallet.getRechargeFrequency();
        double totalTransactions= receivedAmount+sentAmount+rechargedAmount;

        double maxLoan=1_000_000.0;
        double baseLoan=50000.0;
        double bonus=1000.0;
        double wightOfTransactions=0.1;
        double incrementRepayment=1*0.5*baseLoan;

        double transactionVolume=1*totalTransactions*wightOfTransactions;
        double incrementFromRechargeFrequency=1*rechargeFrequency*bonus;
        double calculatedLoan=baseLoan+incrementRepayment+transactionVolume+incrementFromRechargeFrequency;
        Double loanLimit=Math.min(maxLoan,calculatedLoan);
        log.info("loanLimit: {}",loanLimit);



        if (payload == null) {
            payload = new HashMap<>();
            payload.put("step", "amount");
            redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));
            log.info("Show PAYLOAD 1: {}", payload);

            return "CON Your Loan Limit Is: "+loanLimit+"Rwf"+ "\nEnter Loan Amount:";
        }


        String step = payload.get("step");

        switch (step) {
            case "amount":
                if (inputs.length > 1) {
                    String amount = inputs[inputs.length - 1];
                    log.info("After entering amount: {}", amount);
                    try {
                        double parsedAmount = Double.parseDouble(amount);
                        if (parsedAmount <= 0 || parsedAmount>loanLimit) {
                            return "CON Invalid amount. Please Enter a valid Amount between 0-"+loanLimit+"Rwf";
                        }

                        payload.put("amount", amount);


                int repaymentPeriod;
                if (parsedAmount<150000){
                    repaymentPeriod=1;
                }else if (parsedAmount<300000){
                    repaymentPeriod=2;
                } else if (parsedAmount>600000) {
                    repaymentPeriod=3;
                }else {
                    return "CON Please Enter a valid Amount:";
                }
                        payload.put("period", String.valueOf(repaymentPeriod));

                        Loan existingLoan = loanService.getLoanByMsisdn(msisdn);
                        log.info("Existing Loan: {}",existingLoan);
                        if (existingLoan == null || existingLoan.getId() == null) {

                            Double loanInterest=(Double.parseDouble(payload.get("amount"))*0.09)+Double.parseDouble(payload.get("amount"));
                            LoanRequest loanRequest = LoanRequest.builder()
                                    .amount(String.valueOf(loanInterest))
                                    .repaymentPeriod(repaymentPeriod)
                                    .build();
                            log.info("Saving new loan with: {}", loanRequest);
                            LoanResponse loanResponse = loanService.applyForLoan(msisdn, loanRequest);
                            redisTemplate.delete(sessionId);
                            return "END " + loanResponse.getMessage();
                        } else {

                            double updatedAmount = Double.parseDouble(existingLoan.getAmount()) + Double.parseDouble(payload.get("amount"));
                            existingLoan.setAmount(String.valueOf(updatedAmount));
                            existingLoan.setRepaymentPeriod(repaymentPeriod);
                            existingLoan.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                            existingLoan.setStatus(LoanStatus.APPROVED);

                            Loan updatedLoan = loanService.updateLoan(msisdn, existingLoan);
                            log.info("Updated existing loan: {}", updatedLoan);
                            redisTemplate.delete(sessionId);
                            return "END Loan Updated successfully!" ;
                        }
                    } catch (NumberFormatException e) {
                        return "CON Invalid repayment period format. Please enter a numeric value:";
                    }
                } else {
                    return "CON Repayment period cannot be empty. Please enter Repayment Period in months:";
                }

            default:
                redisTemplate.delete(sessionId);
                return "END Invalid input. Please try again.";
        }
    }


private String handleRepayment(String sessionId, String msisdn, String[] inputs) {
    HashMap<String, String> payload = (HashMap<String, String>) redisTemplate.opsForValue().get(sessionId);
    if (payload == null) {
        payload = new HashMap<>();
        payload.put("step", "repayment");
        redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));
        log.info("Initialized repayment session for MSISDN: {}", msisdn);
        return "CON Enter Repayment Amount:";
    }

    String step = payload.get("step");
    log.info("Current repayment step: {}", step);

    switch (step) {
        case "repayment":
            if (inputs.length > 1) {
                String repaymentAmountStr = inputs[inputs.length - 1].trim();
                log.info("Repayment amount entered: {}", repaymentAmountStr);

                if (!repaymentAmountStr.matches("\\d+")) {
                    return "CON Invalid amount. Please enter a valid numeric value:";
                }

                payload.put("repayment", repaymentAmountStr);
                redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));

                Loan loan;
                try {

                    loan = loanService.getRepaymentsByMsisdn(msisdn);
                    log.info("Fetched Loan for repayment: {}", loan);
                } catch (Exception e) {
                    log.error("Error fetching loan for MSISDN {}: {}", msisdn, e.getMessage());
                    return "END No active loan found for your account.";
                }

                if (loan == null || loan.getId() == null) {
                    return "END No active loan found for your account.";
                }

                double repaymentAmount = Double.parseDouble(repaymentAmountStr);
                double remainingAmount = Double.parseDouble(loan.getRemainingAmount());

                log.info("Current Remaining Amount: {}", remainingAmount);


                if (repaymentAmount > remainingAmount) {
                    log.info("Invalid repayment: {}", repaymentAmount);
                    return "CON Amount exceeds the remaining loan amount (" + remainingAmount + " Rwf). Please enter a valid amount.";
                }

                remainingAmount -= repaymentAmount;
                loan.setRemainingAmount(String.valueOf(remainingAmount));
                log.info("Loan before update: {}", loan);
                Loan updatedLoan = loanService.updateLoan(loan);
                log.info("Loan after update: {}", updatedLoan);


                Repayment repaymentRecord = loanService.getRepayment(loan.getId());
                if (repaymentRecord == null) {
                    log.error("No repayment record found for loan ID: {}", loan.getId());
                    return "END Repayment record not found. Contact support.";
                }


                repaymentRecord.setPaidAmount(String.valueOf(
                        Double.parseDouble(repaymentRecord.getPaidAmount()) + repaymentAmount));
                repaymentRecord.setRemainingAmount(String.valueOf(remainingAmount));
                repaymentRecord.setPaymentDate(LocalDate.now());



                if (remainingAmount == 0.0) {


                    loan.setStatus(LoanStatus.PAID);
                    loan.setRemainingAmount(String.valueOf(remainingAmount));
                    loan.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    repaymentRecord.setPaymentStatus(RepaymentStatus.PAID);
                    repaymentRecord.setRemainingAmount(String.valueOf(remainingAmount));

                    log.info("Marking loan and repayment as PAID. {}",repaymentRecord);

                    log.info("Loan before update: {}", loan);
                    loanService.updateLoan(loan);
                    log.info("Loan updated successfully.");
                }
                if (Double.parseDouble(repaymentRecord.getRemainingAmount())==0){
                    log.info("Updating Repayment Status: {}",repaymentRecord);
                    repaymentRecord.setPaymentStatus(RepaymentStatus.PAID);
                }

                loanService.updateRepayment(repaymentRecord);
                log.info("Updated repayment record: {}", repaymentRecord);


                redisTemplate.delete(sessionId);

                return "END " + repaymentAmount + " Rwf paid successfully! " +
                        "Remaining Amount is " + remainingAmount + " Rwf.";
            } else {
                return "CON Repayment Amount cannot be empty. Please enter the amount:";
            }

        default:
            log.warn("Invalid step for repayment session: {}", step);
            redisTemplate.delete(sessionId);
            return "END Invalid input.";
    }
}

    private String handleLoanStatus(String sessionId, String msisdn) {
        HashMap<String, String> payload = (HashMap<String, String>) redisTemplate.opsForValue().get(sessionId);

        if (payload == null) {
            payload = new HashMap<>();
            payload.put("step", "checkLoan");
            redisTemplate.opsForValue().set(sessionId, payload, Duration.ofMinutes(30));
            log.info("CHECK MSISDN: {}", msisdn);
            log.info("CHECK SESSION ID: {}", sessionId);

            try {

               Loan repayments = loanService.getRepaymentsByMsisdn(msisdn);


                if (repayments == null) {
                    log.info("No active Loan for MSISDN: {}", msisdn);
                    return "END No active loan Found.";
                }

                log.info("Fetched repayments: {}", repayments);




                StringBuilder responseBuilder = new StringBuilder("END --LOAN STATUS--");


                    double paidAmount = Double.parseDouble(repayments.getAmount()) - Double.parseDouble(repayments.getRemainingAmount());
                    responseBuilder.append("\n")
                            .append("Loan Amount: ").append(repayments.getAmount()).append("Rwf\n")
                            .append("Paid Amount: ").append(paidAmount).append("Rwf\n")
                            .append("Remaining Amount: ").append(repayments.getRemainingAmount()).append("Rwf\n");

                log.info("CHECK LOAN STATUS RESPONSE: {}", responseBuilder);
                return responseBuilder.toString().trim();




            } catch (Exception e) {
                log.error("Error while fetching loan status", e);
                return "END Failed to fetch loan status. Please try again later.";
            }
        }
        return null;
    }




}
