package de.tuberlin.tfdacmacs.centralserver.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BasicAuthSecurityConfig extends WebSecurityConfigurerAdapter {

    private final CredentialConfig credentialConfig;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(credentialConfig.getUsername()).password(passwordEncoder().encode(credentialConfig.getPassword()))
                .roles("ADMIN")
                .authorities("ROLE_ADMIN");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                    .antMatchers("/authorities")
                .and()
                    .authorizeRequests().anyRequest().authenticated()
                .and()
                    .httpBasic()
                        .realmName("Central Server")
                .and()
                    .csrf().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
