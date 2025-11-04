package com.mera.apirest.utils;

import com.mera.apirest.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "dbdfb4c8981cd059da448979260b819b8c60d1f3c3401e221d92096ad2d0adc55dd1826bec75303ef83f34e776fc869e323888c8986e9e976893396e991acf5e9243b74a2fd6062636e855b873fc3bb176d969e416c8332df927e413df0d35d2f76b7fd7a687751f76df89de7967049a7483ef6b7a8cf48e86d5f5344fd0f9a4e2b428662a3f842fce2d6ec5b9766c514187aa518e658527f58e8804c7bafbc5dd01b01fdf0b27840b13f5c120bcfe13f1c44b29346e6e4a77e3b3785a2047a4125e6d9ec673fa5ee683d6372736eaa1ca6619eb34aca8fd942275436d74447e47b43f3826fc1a509f05e4fd59bd80aed0f5f54261e2b7395fd87f8a9ab31e79"
                    .getBytes(StandardCharsets.UTF_8)
    );

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String generateToken(User user) {
        long expirationMillis = 1000 *60 * 60 * 24; // 24 HORA
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

}

