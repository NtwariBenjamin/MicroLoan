package com.example.userManagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class SessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void setMenuLevel(String sessionId, String menuLevel) {
        redisTemplate.opsForValue().set(sessionId + ":menuLevel", menuLevel, Duration.ofMinutes(30));
        log.info("Menu level '{}' set for session '{}'", menuLevel, sessionId);
    }

    public String getMenuLevel(String sessionId) {

        String menuLevel=  "MAIN_MENU";
        Object menuLevelObj =  redisTemplate.opsForValue().get(sessionId + ":menuLevel");
        if(menuLevelObj != null){
            menuLevel = (String) menuLevelObj;
        }

        return menuLevel;
    }

    public void saveInput(String sessionId, String key, String value) {
        redisTemplate.opsForHash().put(sessionId, key, value);
    }

    public String getInput(String sessionId, String key) {
        return (String) redisTemplate.opsForHash().get(sessionId, key);
    }

    public void clearSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}

