package com.example.userManagement.service;

import com.example.userManagement.configuration.security.JwtService;
import com.example.userManagement.model.Loan.Loan;
import com.example.userManagement.model.Repayment.Repayment;
import com.example.userManagement.model.Loan.LoanRequest;
import com.example.userManagement.model.request.SmsRequest;
import com.example.userManagement.model.response.LoanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class LoanService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${loan.service.url}")
    private String loanServiceUrl;

    @Autowired
    private JwtService jwtService;

    public LoanResponse applyForLoan(String msisdn,LoanRequest loanRequest) {
        String url = loanServiceUrl + "/loan/apply";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService()); // Adjust if needed

        loanRequest.setMsisdn(msisdn);
        HttpEntity<LoanRequest> requestEntity = new HttpEntity<>(loanRequest, headers);

        ResponseEntity<LoanResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, LoanResponse.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Failed to apply for a loan: " + responseEntity.getStatusCode());
        }
    }
    public Loan updateLoan(String msisdn,Loan loanRequest) {
        String url = loanServiceUrl + "/loan/update";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());

        loanRequest.setMsisdn(msisdn);
        HttpEntity<Loan> requestEntity = new HttpEntity<>(loanRequest, headers);

        ResponseEntity<Loan> responseEntity = restTemplate.postForEntity(url, requestEntity, Loan.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Failed to apply for a loan: " + responseEntity.getStatusCode());
        }
    }

    public String checkLoanStatus(String msisdn) {

        String url = loanServiceUrl + "/loan/" +msisdn;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try{
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            log.info("Raw API Response: {}", rawResponse.getBody());

            if (rawResponse.getStatusCode().is2xxSuccessful()) {
                return rawResponse.getBody();
            } else {
            throw new RuntimeException("Unexpected status code: " + rawResponse.getStatusCode());
        }
        }catch (Exception e) {
            log.error("Error while fetching loan status: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch loan status. Please try again later.");
        }
    }

    public Repayment repayLoan(Repayment repaymentRequest) {
        String url = loanServiceUrl + "/loan/repay";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());
        HttpEntity<Repayment> requestEntity = new HttpEntity<>(repaymentRequest, headers);

        ResponseEntity<Repayment> responseEntity = restTemplate.postForEntity(url, requestEntity, Repayment.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Failed to repay loan: " + responseEntity.getStatusCode());
        }
    }

    public String checkOutstandingBalance(String msisdn) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<LoanResponse> response = restTemplate.exchange(
                    loanServiceUrl + "/loan/outstanding-balance?msisdn=" + msisdn,
                    HttpMethod.GET,
                    entity,
                    LoanResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return "Your outstanding loan balance is: " + response.getBody().getLoan();
            } else {
                return "Could not fetch loan balance. Please try again later.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while fetching the loan balance. Please try again.";
        }
    }
    public Loan getLoanByMsisdn(String msisdn) {
        try {

            String url = loanServiceUrl + "/loan/repayment/" + msisdn;


            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Loan> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Loan.class
            );

            log.info("Response Body: {}",responseEntity.getBody());
            if (responseEntity.getStatusCode().is2xxSuccessful() ) {
                return responseEntity.getBody();
            } else {
                throw new RuntimeException("Loan not found for MSISDN: " + msisdn);
            }
        } catch (Exception e) {

            throw new RuntimeException("Error while fetching loan details: " + e.getMessage());
        }
    }
    public Loan getLoanById(Long loanId) {
        try {
            String url = loanServiceUrl + "/loan/" + loanId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<LoanResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    LoanResponse.class
            );
            log.info("Response Status Code: {}", responseEntity.getStatusCode());
            log.info("Response Body: {}", responseEntity.getBody());

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody().getLoan();
            } else {
                throw new RuntimeException("Loan not found for Loan ID: " + loanId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while fetching loan details: " + e.getMessage());
        }
    }
    public Loan updateLoan(Loan loan) {
        try{
        String url=loanServiceUrl+"/loan/update/"+loan.getId();
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+jwtService.generateTokenForService());
        HttpEntity<Loan> requestEntity=new HttpEntity<>(loan,headers);

        ResponseEntity<Loan> responseEntity=restTemplate.exchange(url,HttpMethod.PUT,requestEntity,Loan.class);

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody()!=null){
            log.info("Loan updated successfully: {}", responseEntity.getBody());
            return responseEntity.getBody();
        }else {
            throw new RuntimeException("Failed to update loan. Status: " + responseEntity.getStatusCode());
        }
    }catch (Exception e){
            log.error("Error while updating loan: {}", loan.getId(), e);
            throw new RuntimeException("Error updating Loan"+e.getMessage());
        }
    }

    public Repayment getRepayment(Long loanId) {
        String url = loanServiceUrl+"/loan/repayment/latest/"+loanId;

        HttpHeaders headers=new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Repayment> responseEntity=restTemplate.exchange(url,HttpMethod.GET,requestEntity,Repayment.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()){
            return responseEntity.getBody();
        }else {
            return null;
        }

    }
    public void updateRepayment(Repayment repayment) {
        String url = loanServiceUrl + "/loan/repayment/update";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());
        HttpEntity<Repayment> requestEntity = new HttpEntity<>(repayment, headers);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update repayment record.");
        }
    }

public Loan getRepaymentsByMsisdn(String msisdn) {
    String url = loanServiceUrl + "/loan/repayment/" + msisdn;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());

    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    try {
        ResponseEntity<LoanResponse> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                LoanResponse.class
        );

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
           return responseEntity.getBody().getLoan();
        } else {
            log.error("Failed to fetch repayment records. Status: {}", responseEntity.getStatusCode());
            throw new RuntimeException("Failed to fetch repayment records for MSISDN: " + msisdn);
        }
    } catch (Exception e) {
        log.error("Error fetching repayment records for MSISDN: {}", msisdn, e);
        throw new RuntimeException("Error fetching repayment records: " + e.getMessage());
    }
}

    public void sendSms(String msisdn, String message) {
        String smsUrl = loanServiceUrl + "/sms/send-sms";

        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setMsisdn(msisdn);
        smsRequest.setMessage(message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());

        HttpEntity<SmsRequest> requestEntity = new HttpEntity<>(smsRequest, headers);

        try {
            ResponseEntity<String> smsResponse = restTemplate.exchange(
                    smsUrl, HttpMethod.POST, requestEntity, String.class
            );

            if (smsResponse.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent to {}: {}", msisdn, message);
            } else {
                log.error("Failed to send SMS: {}", smsResponse.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", msisdn, e.getMessage());
        }
    }

}

