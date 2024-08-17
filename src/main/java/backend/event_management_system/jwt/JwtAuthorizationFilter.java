package backend.event_management_system.jwt;

import backend.event_management_system.models.Users;
import backend.event_management_system.service.UserServiceInterface;
import backend.event_management_system.service.UsersService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
        private final JwtTokenProvider jwtTokenProvider;
        private final UserServiceInterface serviceInterface;

        @Autowired
    public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider, @Lazy UserServiceInterface serviceInterface) {
            this.jwtTokenProvider = jwtTokenProvider;
            this.serviceInterface = serviceInterface;
        }

        @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getJwtFromRequest(request);
        if (token != null) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users userDetails = (Users) serviceInterface.loadUserByUsername(email);

            System.out.println("Extracted email from token: " + email);
            System.out.println("Loaded UserDetails: " + userDetails);

            if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                System.out.println("Token is valid.");
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                System.out.println("Token is invalid.");
            }
        }

        filterChain.doFilter(request, response);
    }


    private String getJwtFromRequest(HttpServletRequest request){
        String bearerPart = request.getHeader("Authorization");
        if (bearerPart != null && bearerPart.startsWith("Bearer ")){
           return bearerPart.substring(7);
        }
        return null;
    }
}
