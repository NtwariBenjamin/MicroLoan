package com.example.userManagement.repository;

import com.example.userManagement.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByMsisdn(String username);

    void deleteByMsisdn(String msisdn);
}
