package org.example;


import org.filter.CustowJWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
    @Autowired
    private CustowJWTFilter filter;

    @Autowired
    private AuthenticationProvider provider;

    @Autowired
    private CorsConfigurationSource source;





    @Bean
    public SecurityFilterChain getSecurityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder managerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        managerBuilder.authenticationProvider(provider);
        AuthenticationManager manager = managerBuilder.build();
        http.cors(cor->cor.configurationSource(source));
        http.authorizeHttpRequests(req->req.requestMatchers("/v1/activation/**").authenticated()
                .requestMatchers("/v1/Dashboard/**").authenticated()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/v1/frontPage/**").permitAll()
                .requestMatchers("/v1/payment/**").permitAll()
                .requestMatchers("/v1/google_signin/**").permitAll()
                .requestMatchers("/github/oauth2/**").permitAll()

                .anyRequest().permitAll());
        http.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authenticationManager(manager).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(header-> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }
}
