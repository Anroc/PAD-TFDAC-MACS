package de.tuberlin.tfdacmacs.centralserver.security.config;

import de.tuberlin.tfdacmacs.centralserver.authority.AttributeAuthorityService;
import de.tuberlin.tfdacmacs.centralserver.security.data.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CertificateSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AttributeAuthorityService attributeAuthorityService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    // swagger resources
                    .antMatchers(HttpMethod.GET,"/swagger-ui.html").permitAll()
                    .antMatchers(HttpMethod.GET,"/v2/api-docs").permitAll()
                    // device registration
                    .antMatchers("/certificates/**").permitAll()
                    .anyRequest().authenticated()
                .and()
                    .x509()
                        .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                        .userDetailsService(userDetailsService())
                .and()
                    .csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html", "/webjars/**")
                .and().ignoring().antMatchers("/certificates/**");
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return commonName -> {
            String role = Role.USER.getRoleName();

            if(attributeAuthorityService.exist(commonName)) {
                role = Role.AUTHORITY.getRoleName();
            }

            log.info("Certificate authentication of [{}] role [{}]", commonName, role);

            return new User(commonName, "",
                    AuthorityUtils.commaSeparatedStringToAuthorityList(role));
        };
    }


}
