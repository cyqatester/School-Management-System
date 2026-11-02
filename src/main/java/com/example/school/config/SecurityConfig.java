package com.example.school.config;

import com.example.school.model.User;
import com.example.school.service.UserService;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UserService users) {
    return name -> {
      User u = users.byUsername(name).orElseThrow(() -> new UsernameNotFoundException(name));
      return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
          .password(u.getPasswordHash()).roles(u.getRole().name()).build();
    };
  }

  @Bean
  AuthenticationProvider authenticationProvider(UserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider(uds);
    p.setPasswordEncoder(encoder);
    return p;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(a -> a
        .requestMatchers("/login", "/css/**", "/error").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .requestMatchers("/teacher/**").hasRole("TEACHER")
        .anyRequest().authenticated())
        .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
        .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll()).build();
  }
}
