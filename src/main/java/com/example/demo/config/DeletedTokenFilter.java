package com.example.demo.config;

import com.example.demo.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class DeletedTokenFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // 먼저 토큰이 삭제되었는지 확인
            if (tokenService.isTokenDeleted(token)) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.getWriter().write("삭제된 토큰입니다");
                return;
            }
            
            // 토큰 유효성 검사
            if (!tokenService.isValidToken(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("유효하지 않은 토큰입니다");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
} 