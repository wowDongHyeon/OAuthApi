package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.ClientService;
import com.example.demo.service.TokenService;
import com.example.demo.model.Client;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerClient(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        Client client = clientService.registerClient(ipAddress);
        
        Map<String, String> response = new HashMap<>();
        response.put("clientId", client.getClientId());
        response.put("clientSecret", client.getClientSecret());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token")
    public ResponseEntity<?> getToken(
                @RequestParam(name = "clientId") String clientId, 
                @RequestParam(name = "clientSecret") String clientSecret) {
        Client client = clientService.validateClient(clientId);
        
        if (!client.getClientSecret().equals(clientSecret)) {
            return ResponseEntity.badRequest().body("Invalid client credentials");
        }
        
        String token = tokenService.generateToken(clientId);
        
        Map<String, String> response = new HashMap<>();
        response.put("access_token", token);
        response.put("token_type", "Bearer");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok("Access granted!");
    }
    
    @GetMapping("/token/remaining-time")
    public ResponseEntity<?> getTokenRemainingTime(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid authorization header");
        }
        
        String token = authHeader.substring(7); // "Bearer " 제거
        long remainingSeconds = tokenService.getRemainingTime(token);
        
        Map<String, Object> response = new HashMap<>();
        if (remainingSeconds < 0) {
            response.put("error", "Invalid or expired token");
            return ResponseEntity.badRequest().body(response);
        }
        
        response.put("remaining_seconds", remainingSeconds);
        return ResponseEntity.ok(response);
    }
} 