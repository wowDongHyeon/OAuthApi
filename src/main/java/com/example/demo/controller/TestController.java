package com.example.demo.controller;

import com.example.demo.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/apis")
public class TestController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        // 현재 토큰의 남은 시간 확인
        long remainingTime = tokenService.getRemainingTime(authentication);
        response.put("remainingTime", remainingTime);
        
        // 토큰 갱신
        String newToken = tokenService.refreshToken(authentication);
        response.put("newToken", newToken);
        
        // 새로운 토큰의 유효시간 확인
        long newRemainingTime = tokenService.getRemainingTimeFromToken(newToken);
        response.put("newRemainingTime", newRemainingTime);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.deleteToken(token);
            Map<String, String> deletedTokenInfo = tokenService.getDeletedTokenInfo(token);
            return ResponseEntity.ok(deletedTokenInfo);
        }
        return ResponseEntity.badRequest().body("Invalid Authorization header");
    }
} 