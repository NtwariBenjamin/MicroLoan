package com.example.userManagement.controller;

import com.example.userManagement.configuration.security.JwtService;
import com.example.userManagement.model.response.LoginResponse;
import com.example.userManagement.model.response.UserResponse;
import com.example.userManagement.exception.UserNotFoundException;
import com.example.userManagement.model.user.LoginRequest;
import com.example.userManagement.model.user.User;
import com.example.userManagement.model.user.UserRequest;
import com.example.userManagement.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("auth/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse;
        try {
            log.info("Registering User with Request: {}", userRequest);
            userResponse = userService.registerUser(userRequest);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            log.error("Error Registering User: {}", userRequest, e);
            throw new RuntimeException("Error Creating new User");
        }
    }

    @PostMapping("auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = null;
        String token;

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getMsisdn(), loginRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            token = jwtService.generateToken(userDetails);
            loginResponse = LoginResponse.builder()
                    .token(token)
                    .message("Customer Logged In!")
                    .token(token)
                    .build();
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/borrower/{msisdn}")
    public ResponseEntity<UserResponse> getUserByMsisdn(@PathVariable String msisdn) {
        UserResponse userResponse;
        try {
            log.info("Getting User By msisdn: {}", msisdn);
            userResponse = userService.getUserByMsisdn(msisdn);
            log.info("Getting User By UserResponse: {}", userResponse);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            log.error("Error retrieving User with msisdn: {}", msisdn, e);
            throw new RuntimeException("Error retrieving User");
        } catch (Exception e) {
            log.warn("User with this msisdn: {} not found", msisdn, e);
            throw new UserNotFoundException("User with msisdn: " + msisdn + "not Found");

        }
    }

    @PutMapping("/borrower/update/{msisdn}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String msisdn,
                                                   @RequestBody UserRequest userRequest) {
        UserResponse userResponse;
        try {
            log.info("Updating User with Request: {}", userRequest);
            userResponse = userService.updateUser(msisdn, userRequest);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            log.error("Error Updating User with Request: {}", msisdn, e);
            throw new RuntimeException("Error Updating User", e);
        } catch (Exception e) {
            log.warn("User with msisdn: {} not found", msisdn, e);
            throw new UserNotFoundException("User Not Found");
        }
    }

    @DeleteMapping("/admin/deleteUser/{msisdn}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable String msisdn) {
        UserResponse userResponse;
        try {
            log.info("Deleting User By msisdn: {}", msisdn);
            userResponse = userService.deleteUser(msisdn);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            log.error("Error Deleting User with msisdn: {}", msisdn, e);
            throw new RuntimeException("Error Deleting User with msisdn: " + msisdn, e);
        } catch (Exception e) {
            log.warn("User with msisdn: {} not found", msisdn, e);
            throw new UserNotFoundException("User with msisdn: " + msisdn + " not found");
        }
    }

    @GetMapping("/admin/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            log.info("Retrieving All Users");
            List<User> usersResponse = userService.getAllUsers();
            return ResponseEntity.ok(usersResponse);
        } catch (Exception e) {
            log.error("Error Retrieving Users", e);
            throw new RuntimeException("Error Retrieving Users", e);
        }
    }
}
