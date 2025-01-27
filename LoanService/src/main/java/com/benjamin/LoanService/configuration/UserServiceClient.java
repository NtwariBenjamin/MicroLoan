package com.benjamin.LoanService.configuration;

import com.benjamin.LoanService.model.user.User;
import com.benjamin.LoanService.model.user.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
@Slf4j
public class UserServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.management.service.url}")
    private String userServiceUrl;
    @Autowired
    private JwtService jwtService;
    public User getUserDetails(String msisdn) {

        if (msisdn == null || msisdn.isEmpty()) {
            throw new IllegalArgumentException("MSISDN cannot be null or empty");
        }
        String url = UriComponentsBuilder
                .fromHttpUrl(userServiceUrl)
                .pathSegment("api","borrower",msisdn)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtService.generateTokenForService());
        HttpEntity<String> entity = new HttpEntity<>(headers);


        log.info("Calling URL: {}", url);
        ResponseEntity<UserResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserResponse.class);
        log.info("Raw Response: {}", response.getBody().getUser());
        return response.getBody().getUser();
    }
}