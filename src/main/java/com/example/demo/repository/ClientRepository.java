package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.demo.model.Client;
import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String> {
    Optional<Client> findByClientId(String clientId);
    Optional<Client> findByIpAddress(String ipAddress);
} 