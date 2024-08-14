package backend.event_management_system.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String jwtSecret;
    @Value("${jwt.expirationMs}")
    private int expirationMs;

    public String generateToken(Authentication auth){
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(new Date().getTime() + expirationMs))
                .withClaim("roles", roles)
                .sign(Algorithm.HMAC256(jwtSecret.getBytes()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String usernameFromToken = getUsernameFromToken(token);
        return (usernameFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getUsernameFromToken(String token){
        return JWT.require(Algorithm.HMAC256(jwtSecret.getBytes()))
                .build()
                .verify(token)
                .getSubject();
    }

    private boolean isTokenExpired(String token){
        return JWT.require(Algorithm.HMAC256(jwtSecret.getBytes()))
                .build()
                .verify(token)
                .getExpiresAt()
                .before(new Date());
    }

}
