package de.tuberlin.tfdacmacs.attributeauthority.security.config;

import de.tuberlin.tfdacmacs.attributeauthority.authority.TrustedAuthorityService;
import de.tuberlin.tfdacmacs.attributeauthority.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CertificateSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final TrustedAuthorityService trustedAuthorityService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                    // endpoint for users
                    .antMatchers(HttpMethod.GET,"/authority")
                    // endpoint for other AA
                    .antMatchers(HttpMethod.GET, "/users/*/devices/*")
                .and()
                    .authorizeRequests().anyRequest().authenticated()
                .and()
                    .x509()
                        .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                        .userDetailsService(userDetailsService())
                .and()
                    .csrf().disable();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return commonName -> {

            String role = "ROLE_EXTERN";
            if(userService.existUser(commonName)) {
                role = "ROLE_USER";
            } else if(trustedAuthorityService.existTrustedAuthority(commonName)) {
                role = "ROLE_AUTHORITY";
            }

            log.info("Certificate authentication of [{}] role [{}]", commonName, role);

            return new User(commonName, "",
                    AuthorityUtils.commaSeparatedStringToAuthorityList(role));
        };
    }


}
