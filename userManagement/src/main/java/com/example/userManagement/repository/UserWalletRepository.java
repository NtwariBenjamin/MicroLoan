package com.example.userManagement.repository;

import com.example.userManagement.model.user.UserWalletActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWalletActivity,Long> {
    Optional<UserWalletActivity> findByMsisdn(String msisdn);
}
