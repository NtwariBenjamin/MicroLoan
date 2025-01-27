package com.example.userManagement.controller;

import com.example.userManagement.model.request.SmsRequest;
import com.example.userManagement.service.LoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private LoanService loanService;


    @PostMapping("/send")
    public ResponseEntity<String> sendSmsToUser(@RequestBody SmsRequest smsRequest){
        try {
            log.info("Check Usage!!");
            loanService.sendSms(smsRequest.getMsisdn(),smsRequest.getMessage());
            return ResponseEntity.ok("SMS sent Successfully!");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed To Send Sms");
        }
    }
}
