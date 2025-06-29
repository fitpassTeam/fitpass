package org.example.fitpass.common.security;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.jwt.JwtAuthenticationFilter;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.common.oAuth2.CustomOAuth2UserService;
import org.example.fitpass.common.oAuth2.OAuth2SuccessHandler;
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
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final CustomUserDetailsService customUserDetailsService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RedirectUrlCookieFilter redirectUrlCookieFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // 공개 API (GET /gyms 포함)
                .requestMatchers(HttpMethod.GET, "/gyms").permitAll()
                .requestMatchers(HttpMethod.POST, "/images").permitAll()
                .requestMatchers(HttpMethod.GET, "/gyms/{gymId}/trainers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/gyms/{gymId}/memberships/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(
                    "/auth/**",
                    "/login",
                    "/ws/**",
                    "/error",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/search/**",
                    "/memberships/purchases/**"
                ).permitAll()
                // 관리자만 접근 가능
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            // JWT 인증 필터 추가
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, redisService, customUserDetailsService),
                UsernamePasswordAuthenticationFilter.class
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}");
                })
            )

            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
            )
            // OAuth2 redirect 필터 추가
            .addFilterBefore(redirectUrlCookieFilter, OAuth2AuthorizationRequestRedirectFilter.class)
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 주소 허용 (필요에 따라 추가)
        configuration.setAllowedOrigins(List.of(
            "http://127.0.0.1:5500",
            "http://localhost:5173",
            "https://www.fitpass-13.com",
            "https://fitpass-13.com"
        ));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
