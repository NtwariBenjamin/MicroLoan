package com.benjamin.LoanService.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class SmsService {


    @Value("${africastalking.apiKey}")
    private String apiKey;

    @Value("${africastalking.username}")
    private String username;

    @Value("${africastalking.senderId}")
    private String senderId;


    public void sendSms(String msisdn, String message) {
        try {
            ensureTLS12();

            String apiUrl = "https://api.sandbox.africastalking.com/version1/messaging";

            String payload = String.format("username=%s&to=%s&message=%s&from=%s",
                    URLEncoder.encode(username, StandardCharsets.UTF_8),
                    URLEncoder.encode(msisdn, StandardCharsets.UTF_8),
                    URLEncoder.encode(message, StandardCharsets.UTF_8),
                    URLEncoder.encode(senderId, StandardCharsets.UTF_8));

            log.info("Sending SMS Payload: {}", payload);

            URL url = new URL(apiUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("apikey", apiKey);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            log.info("Response Code: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {

                InputStream inputStream = connection.getInputStream();
                if (inputStream != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line.trim());
                        }
                        log.info("SMS sent successfully! Response: {}", response);
                    }
                } else {
                    log.info("SMS sent successfully! No response body.");
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    log.error("Failed to send SMS. Response Code: {}, Error Response: {}", responseCode, errorResponse);
                }
            }
        } catch (Exception e) {
            log.error("Error sending SMS: {}", e.getMessage(), e);
            e.printStackTrace();
        }
    }


    private void ensureTLS12() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, new java.security.SecureRandom());
            SSLContext.setDefault(sslContext);
            log.info("TLSv1.2 has been enabled successfully.");
        } catch (Exception e) {
            log.error("Error enabling TLSv1.2: {}", e.getMessage(), e);
        }
    }
}