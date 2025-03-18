package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.model.Client;
import com.example.demo.repository.ClientRepository;
import java.util.UUID;

@Service
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;
    
    public Client registerClient(String ipAddress) {
        // Check if IP is already registered
        if (clientRepository.findByIpAddress(ipAddress).isPresent()) {
            throw new RuntimeException("IP address already registered");
        }
        
        // Generate client credentials
        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();
        
        // Create and save new client
        Client client = Client.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .ipAddress(ipAddress)
                .active(true)
                .scopes(new String[]{"read", "write"})
                .build();
                
        return clientRepository.save(client);
    }
    
    public Client validateClient(String clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }
} 