package com.benjamin.LoanService.controller;

import com.benjamin.LoanService.configuration.UserServiceClient;
import com.benjamin.LoanService.model.user.User;
import com.benjamin.LoanService.model.request.SmsRequest;
import com.benjamin.LoanService.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private SmsService smsService;

    @PostMapping("/send-sms")
    public ResponseEntity<String> sendSms(@RequestBody SmsRequest smsRequest,@RequestHeader("Authorization") String authToken) {
        String msisdn=smsRequest.getMsisdn();
        User userResponse = userServiceClient.getUserDetails(msisdn);
        try {
            smsService.sendSms(userResponse.getMsisdn(), smsRequest.getMessage());
            return ResponseEntity.ok("SMS sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send SMS");
        }
    }
}
