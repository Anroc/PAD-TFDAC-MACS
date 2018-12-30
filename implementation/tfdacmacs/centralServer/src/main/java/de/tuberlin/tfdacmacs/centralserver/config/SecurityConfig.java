package de.tuberlin.tfdacmacs.centralserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CredentialConfig credentialConfig;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(credentialConfig.getUsername()).password(passwordEncoder().encode(credentialConfig.getPassword()))
                .authorities("ROLE_ADMIN");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().realmName("Central Server")
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET,"/swagger-ui.html").permitAll()
                .antMatchers(HttpMethod.GET,"/v2/api-docs").permitAll()
                .antMatchers(HttpMethod.PUT, "/users/*").permitAll() // secure only for AA members
                .antMatchers(HttpMethod.POST,"/users").permitAll() // secure only for AA members
                .antMatchers(HttpMethod.GET, "/gpp").permitAll()
                .anyRequest().authenticated();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html", "/webjars/**")
                .and().ignoring().antMatchers("/users/**", "/users");
    }
}
