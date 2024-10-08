package backend.event_management_system.jwt;

import backend.event_management_system.models.Users;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String jwtSecret;
    @Value("${jwt.expirationMs}")
    private int expirationMs;

    public String generateToken(Authentication auth){
        Users userDetails = (Users) auth.getPrincipal();
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return JWT.create()
                .withSubject(userDetails.getEmail())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(new Date().getTime() + expirationMs))
                .withClaim("authorities", authorities)
                .sign(Algorithm.HMAC256(jwtSecret.getBytes()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String emailFromToken = getEmailFromToken(token);
        if (userDetails instanceof Users) {
            String emailFromUsers = ((Users) userDetails).getEmail();
            return (emailFromToken.equals(emailFromUsers) && !isTokenExpired(token));
        } else {
            return false;
        }
    }

    public String getEmailFromToken(String token){
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

    public List<GrantedAuthority> listOfAuthoritiesGrantedToAuthenticatedUser(String token){
        String roles = JWT.require(Algorithm.HMAC256(jwtSecret.getBytes()))
                .build()
                .verify(token)
                .getClaim("authorities").asString();

        return stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
