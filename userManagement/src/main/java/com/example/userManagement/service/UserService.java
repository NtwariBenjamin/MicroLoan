package com.example.userManagement.service;

import com.example.userManagement.model.user.User;
import com.example.userManagement.model.user.UserRequest;
import com.example.userManagement.model.response.UserResponse;
import com.example.userManagement.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;




        public UserResponse registerUser(UserRequest userRequest) {
            Optional<User> existingUser = userRepository.findByMsisdn(userRequest.getMsisdn());
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("User with msisdn " + userRequest.getMsisdn() + " already exists.");
            }

            User user = new User();
            user.setName(userRequest.getName());
            user.setIdNo(userRequest.getId());
            user.setLocation(userRequest.getLocation());
            user.setMsisdn(userRequest.getMsisdn());
            user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
            user.setLoanEligibility(userRequest.getLoanEligibility());
            user.setRole(userRequest.getRole());

            userRepository.save(user);
            return UserResponse.builder()
                .user(user)
                .message("User Registered Successfully!")
                .build();
        }

    @Cacheable(value = "users")
    public UserResponse getUserByMsisdn(String msisdn) {
        log.info("Test 1 {}",msisdn);
        Optional<User> optionalUser=userRepository.findByMsisdn(msisdn);
        log.info("Test 2 {}",optionalUser);
        if(optionalUser.isPresent()){
            return UserResponse.builder()
                    .user(optionalUser.get())
                    .message("User with this msisdn: "+msisdn+" is Found")
                    .build();
        }else {
            return UserResponse.builder()
                    .user(null)
                    .message("User with this msisdn: "+msisdn+" is not Found")
                    .build();
        }
    }

    @CachePut(value = "users",key = "msisdn")
    public UserResponse updateUser(String msisdn, UserRequest userRequest) {

        Optional<User> optionalUser=userRepository.findByMsisdn(msisdn);
        if (userRequest.getPassword().length()<4 || !userRequest.getPassword().equals(userRequest.getConfirmPassword())){
            return UserResponse.builder()
                    .user(null)
                    .message("Password Should be more than 4 characters and should match confirm Password")
                    .build();
        }
        User user;
        if(optionalUser.isEmpty()){
            return UserResponse.builder()
                    .message("User with: "+msisdn+" Not Found")
                    .user(null)
                    .build();
        }
            user=optionalUser.get();
            user.setName(userRequest.getName());
            user.setLocation(userRequest.getLocation());
            user.setRole(userRequest.getRole());
            user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
            user.setLoanEligibility(userRequest.getLoanEligibility());
            userRepository.save(user);


        return UserResponse.builder()
                .user(user)
                .message("User with msisdn: "+msisdn+" Updated Successfully")
                .build();
    }

    public UserResponse deleteUser(String msisdn) {
        Optional<User> optionalUser=userRepository.findByMsisdn(msisdn);
        if(optionalUser.isEmpty()){
            return UserResponse.builder()
                    .message("User with: "+msisdn+" Not Found")
                    .user(null)
                    .build();
        }
        userRepository.deleteByMsisdn(msisdn);
        return UserResponse.builder()
                .user(null)
                .message("User with this msisdn: "+msisdn+" is Deleted Successfully!")
                .build();
    }

    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        log.info("Fetching all users from database...");
        return userRepository.findAll();
    }

}
