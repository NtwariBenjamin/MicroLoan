package com.benjamin.LoanService.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(jwtToken)
                        .getBody();

                String msisdn = claims.get("msisdn", String.class);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, List.of());
                authToken.setDetails(msisdn); // Attach msisdn to authentication token
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (Exception e) {
                logger.error("Invalid JWT Token", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
