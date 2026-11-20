package com.examly.springapp.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {
    private final byte[] secretKey;
    private static final long TOKEN_TTL_MILLIS = 24L * 60 * 60 * 1000;

    public JwtUtils(@Value("${MOVIES_JWT_SECRET:}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("MOVIES_JWT_SECRET must contain at least 32 bytes");
        }
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    }
    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration); }
    public <T> T extractClaim(String token, Function<Claims,T> resolver) {
        return resolver.apply(Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody());
    }
    public Boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && extractExpiration(token).after(new Date());
    }
    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder().setClaims(new HashMap<String,Object>()).setSubject(username)
            .setIssuedAt(now).setExpiration(new Date(now.getTime() + TOKEN_TTL_MILLIS))
            .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }
}
