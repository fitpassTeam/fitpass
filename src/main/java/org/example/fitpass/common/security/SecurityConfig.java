package org.example.fitpass.common.security;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.jwt.JwtAuthenticationFilter;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.oAuth2.CustomOAuth2UserService;
import org.example.fitpass.common.oAuth2.OAuth2SuccessHandler;
import org.example.fitpass.config.RedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;  // 추가!
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/gyms").permitAll()
                .requestMatchers(
                    "/auth/**",
                    "/ws/**",
                    "/error",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/search/**",
                    "/oauth2/**",
                    "/login/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/gyms").permitAll()
                // 관리자만 접근 가능
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, redisService,
                    customUserDetailsService),
                UsernamePasswordAuthenticationFilter.class
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)      // CustomOAuth2UserService 연결, 사용자 정보 처리 서비스
                )
                .successHandler(oAuth2SuccessHandler)          // 성공 핸들러 연결, 로그인 성공 후 처리
                .failureUrl("/login?error=oauth2_failed")      // 실패 시 리다이렉트
            )
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
    }
}
