package de.tuberlin.tfdacmacs.attributeauthority.security.config;

import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class BasicAuthSecurityConfig extends WebSecurityConfigurerAdapter {

    private final CredentialConfig credentialConfig;
    private final AttributeAuthorityConfig config;

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
                .authorizeRequests()
                    // swagger resources
                    .antMatchers(HttpMethod.GET,"/swagger-ui.html").permitAll()
                    .antMatchers(HttpMethod.GET,"/v2/api-docs").permitAll()
                    .anyRequest().authenticated()
                .and()
                    .httpBasic()
                        .realmName(config.getId())
                .and()
                    .csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html", "/webjars/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
