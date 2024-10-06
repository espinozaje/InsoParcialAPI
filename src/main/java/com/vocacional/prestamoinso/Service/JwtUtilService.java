package com.vocacional.prestamoinso.Service;


import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.Trabajador;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class JwtUtilService {
    // Cambia esto a una clave más segura
    private final long EXPIRATION_TIME = 86400000;  // 1 día en milisegundos
    private SecretKey secretKey;

    public JwtUtilService() {
        // Genera una clave segura para HS256
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }


    public String generateToken(Trabajador trabajador) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, trabajador.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
