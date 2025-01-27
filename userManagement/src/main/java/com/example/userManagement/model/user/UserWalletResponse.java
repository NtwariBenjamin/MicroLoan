package com.example.userManagement.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWalletResponse {
    private UserWalletActivity userWalletActivity;
    private String message;
}
