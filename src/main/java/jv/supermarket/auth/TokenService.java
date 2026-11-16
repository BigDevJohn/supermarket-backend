package jv.supermarket.auth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import jv.supermarket.user.User;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Value("${api.security.secret}")
    private String secret;

    public String generateToken(User user) {
        log.debug("[generateToken] Generating JWT token for user ID: {}", user.getId());
        Algorithm algorithm = Algorithm.HMAC256(secret);

        String token = JWT.create()
                .withSubject(user.getEmail())
                .withIssuer("Supermarket API")
                .withExpiresAt(generateExpirationTime())
                .sign(algorithm);
        log.debug("[generateToken] JWT token successfully generated for user ID: {}", user.getId());
        return token;
    }

    public String validateToken(String token) {
        log.debug("[validateToken] Validating JWT token");
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String subject = JWT.require(algorithm)
                    .withIssuer("Supermarket API")
                    .build()
                    .verify(token)
                    .getSubject();
            log.debug("[validateToken] JWT token successfully validated");
            return subject;
        } catch (JWTVerificationException e) {
            log.warn("[validateToken] JWT token verification failed: {}", e.getMessage());
            throw e;
        }
    }

    private Instant generateExpirationTime() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
