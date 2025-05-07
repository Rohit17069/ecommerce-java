package com.bootcamp.ecommerce_rohit.services;

import com.bootcamp.ecommerce_rohit.entities.AccessToken;
import com.bootcamp.ecommerce_rohit.entities.User;
import com.bootcamp.ecommerce_rohit.repositories.AccessTokenRepository;
import com.bootcamp.ecommerce_rohit.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class JwtUtilsService{

    @Autowired
    AccessTokenRepository accessTokenRepository;
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public String generateToken(String username,Integer expiryInMinutes) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
               .setExpiration(new Date(System.currentTimeMillis() + expiryInMinutes * 60 * 1000L))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // Extract Username
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Validate Token
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    // Extract Claims
    private Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

}

