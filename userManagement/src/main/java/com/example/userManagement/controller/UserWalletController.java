package com.example.userManagement.controller;

import com.example.userManagement.model.user.UserWalletRequest;
import com.example.userManagement.model.user.UserWalletResponse;
import com.example.userManagement.service.UserWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@Slf4j
public class UserWalletController {

    @Autowired
    private UserWalletService userWalletService;

    @PostMapping("/add")
    public ResponseEntity<UserWalletResponse> addUserWalletActivity(@RequestBody UserWalletRequest userWalletRequest){
        UserWalletResponse userWalletResponse;
        try {
            log.info("Adding a new User Wallet Activity with Request: {}",userWalletRequest);
            userWalletResponse=userWalletService.addUserWallet(userWalletRequest);
            return ResponseEntity.ok(userWalletResponse);
        }catch (Exception e){
            log.error("Error Adding Wallet Activity with Request: {}",userWalletRequest);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserWalletResponse.builder().message("Error Adding User Wallet Activity").build());
        }
    }

    @GetMapping("/user/{msisdn}")
    public ResponseEntity<UserWalletResponse> getUserWalletByMsisdn(@PathVariable String msisdn){
        UserWalletResponse userWalletResponse;
        try {
            log.info("Getting User Wallet By Msisdn: {}",msisdn);
            userWalletResponse=userWalletService.getUserWalletByMsisdn(msisdn);
            return ResponseEntity.ok(userWalletResponse);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserWalletResponse.builder().message("User Wallet Not Found").build());
        }
    }

}
