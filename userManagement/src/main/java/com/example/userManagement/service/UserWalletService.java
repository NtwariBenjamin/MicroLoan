package com.example.userManagement.service;

import com.example.userManagement.model.user.UserWalletActivity;
import com.example.userManagement.model.user.UserWalletRequest;
import com.example.userManagement.model.user.UserWalletResponse;
import com.example.userManagement.repository.UserWalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserWalletService {
    @Autowired
    private UserWalletRepository userWalletRepository;

    public UserWalletResponse addUserWallet(UserWalletRequest userWalletRequest) {

        Optional<UserWalletActivity> userWalletOptional=userWalletRepository.findByMsisdn(userWalletRequest.getMsisdn());

        UserWalletActivity newUserWallet=UserWalletActivity.builder()
                .msisdn(userWalletRequest.getMsisdn())
                .createdAt(userWalletRequest.getCreatedAt())
                .lastRechargeDate(userWalletRequest.getLastRechargeDate())
                .rechargeFrequency(userWalletRequest.getRechargeFrequency())
                .totalAmountSent(userWalletRequest.getTotalAmountSent())
                .totalAmountReceived(userWalletRequest.getTotalAmountReceived())
                .totalRechargeAmount(userWalletRequest.getTotalRechargeAmount())
                .updatedAt(userWalletRequest.getUpdatedAt())
                .build();
        userWalletRepository.save(newUserWallet);

        return UserWalletResponse.builder()
                .userWalletActivity(newUserWallet)
                .message("New User Added To Wallet Activity")
                .build();
    }

    public UserWalletResponse getUserWalletByMsisdn(String msisdn) {
        Optional<UserWalletActivity> userWalletOptional=userWalletRepository.findByMsisdn(msisdn);
        if (userWalletOptional.isEmpty()){
            log.info("User Wallet Activity Not Found for Msisdn: {}", msisdn);
            return UserWalletResponse.builder()
                    .userWalletActivity(null)
                    .message("User Not Found")
                    .build();
        }

        UserWalletActivity userWallet=userWalletOptional.get();
        return UserWalletResponse.builder()
                .userWalletActivity(userWallet)
                .message("User Wallet Found!")
                .build();
    }
}
