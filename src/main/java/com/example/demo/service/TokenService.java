package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    @Autowired
    private JwtEncoder encoder;
    
    @Autowired
    private JwtDecoder decoder;

    // 삭제된 토큰을 저장할 맵
    private final Map<String, String> deletedTokens = new ConcurrentHashMap<>();

    public String generateToken(String clientId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(100, ChronoUnit.SECONDS))
                .subject(clientId)
                .claim("scope", "api:access")
                .build();
        
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    
    public long getRemainingTime(String token) {
        try {
            var jwt = decoder.decode(token);
            Instant expiration = jwt.getExpiresAt();
            if (expiration == null) {
                return -1; // 토큰에 만료 시간이 없는 경우
            }
            
            Duration remaining = Duration.between(Instant.now(), expiration);
            return remaining.toSeconds();
        } catch (Exception e) {
            return -1; // 토큰이 유효하지 않은 경우
        }
    }

    public long getRemainingTime(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            return getRemainingTime(jwtAuth.getToken().getTokenValue());
        }
        return -1;
    }

    public long getRemainingTimeFromToken(String token) {
        return getRemainingTime(token);
    }

    public org.springframework.security.oauth2.jwt.Jwt getJwt(String token) {
        return decoder.decode(token);
    }

    public String refreshToken(String token) {
        try {
            var jwt = decoder.decode(token);
            Instant now = Instant.now();
            
            // 기존 토큰의 모든 claims를 유지하면서 만료 시간만 갱신
            JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
            jwt.getClaims().forEach((key, value) -> {
                if (!key.equals("exp")) { // exp(만료시간)만 제외하고 모든 claims 유지
                    claimsBuilder.claim(key, value);
                }
            });
            
            // 만료 시간만 새로 설정
            claimsBuilder.expiresAt(now.plus(100, ChronoUnit.SECONDS));
            
            return encoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).getTokenValue();
        } catch (Exception e) {
            throw new RuntimeException("토큰 갱신 실패", e);
        }
    }

    public String refreshToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            return refreshToken(jwtAuth.getToken().getTokenValue());
        }
        throw new RuntimeException("유효하지 않은 인증 토큰");
    }

    public void deleteToken(String token) {
        try {
            var jwt = decoder.decode(token);
            deletedTokens.put(token, jwt.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("토큰 삭제 실패", e);
        }
    }

    public boolean isTokenDeleted(String token) {
        return deletedTokens.containsKey(token);
    }

    public Map<String, String> getDeletedTokenInfo(String token) {
        if (deletedTokens.containsKey(token)) {
            return Map.of(
                "token", token,
                "subject", deletedTokens.get(token)
            );
        }
        return null;
    }

    public boolean isValidToken(String token) {
        try {
            var jwt = decoder.decode(token);
            Instant expiration = jwt.getExpiresAt();
            if (expiration == null) {
                return false;
            }
            return !expiration.isBefore(Instant.now());
        } catch (Exception e) {
            return false;
        }
    }
} 