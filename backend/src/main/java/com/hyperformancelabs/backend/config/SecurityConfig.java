package com.hyperformancelabs.backend.config;

import com.hyperformancelabs.backend.service.impl.CustomAdminDetailsService;
import com.hyperformancelabs.backend.service.impl.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.*;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private AdminLogoutSuccessHandler adminLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository()))
                .securityMatcher("/admin/**") // Áp dụng cho tất cả /admin/*
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/css/**", "/admin/js/**", "admin/precheck").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessHandler(adminLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.
                        ignoringRequestMatchers("/logout/**", "/cart/add/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )

//            .csrf(csrf -> csrf.disable()) // Có thể bật lại CSRF protection cho MVC
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/").permitAll() // Cho phép truy cập trang chủ
                .requestMatchers("/shop/**").permitAll()
                .requestMatchers("/products/**").permitAll()
                .requestMatchers("/cart/**").permitAll() // Cho phép truy cập trang giỏ hàng
                .requestMatchers("/checkout/**").permitAll() // Cho phép truy cập trang thanh toán
                .requestMatchers("/wishlist/**").permitAll() // Cho phép truy cập trang danh sách yêu thích
                .requestMatchers("/order-success/**").permitAll() // Cho phép truy cập trang xác nhận đơn hàng thành công
                .requestMatchers("/profile/**").permitAll() // Cho phép truy cập trang thông tin người dùng
                .requestMatchers("/auth/**").permitAll() // Cho phép truy cập trang đăng nhập/đăng ký
                .requestMatchers("/error/**").permitAll()
                .requestMatchers("/about").permitAll() // Cho phép truy cập trang giới thiệu
                .requestMatchers("/contact").permitAll() // Cho phép truy cập trang liên hệ
                .requestMatchers("/blog/**").permitAll()
                .requestMatchers("/track-order/**").permitAll()
                .requestMatchers("/register/**").permitAll()// Cho phép truy cập trang blog
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
//                .requestMatchers("/admin/**").permitAll()
//                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .defaultSuccessUrl("/") // Chuyển hướng đến trang chủ sau khi đăng nhập thành công
                .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(logoutSuccessHandler)  // Sử dụng handler tùy chỉnh
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public UserDetailsService adminDetailsService() {
        return new CustomAdminDetailsService();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new CookieCsrfTokenRepository();
    }
}