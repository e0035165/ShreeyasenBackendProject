package org.filter;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.entity.CustomUserDetails;
import org.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.utilities.RsaService;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;


@Component
public class CustowJWTFilter extends OncePerRequestFilter {
    @Autowired
    private RsaService rsa_service;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if(request.getRequestURI().contains("/v1/activation")
        || request.getRequestURI().contains("/v1/Dashboard")) {
            String jwt = request.getHeader("Authorization").substring(7);
            Map<String,Object> values = rsa_service.jwtDecrypt(jwt);
            String username = (String) values.get("username");
            String password = (String) values.get("password");
            CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
            if(userDetails!=null &&
                    encoder.matches(password, userDetails.getPassword())
                    && SecurityContextHolder.getContext().getAuthentication()==null) {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(token);
            }
        }
        filterChain.doFilter(request,response);
    }
}
