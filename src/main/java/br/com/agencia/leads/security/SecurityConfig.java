package br.com.agencia.leads.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
