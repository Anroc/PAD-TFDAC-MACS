package de.tuberlin.tfdacmacs.centralserver.security;

import de.tuberlin.tfdacmacs.centralserver.security.data.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationFacade implements IAuthenticationFacade {
 
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getId() {
        return getAuthentication().getName();
    }

    public Role getRole() {
        if(getAuthentication().getAuthorities().size() > 1) {
            log.warn("Expecting only 1 role per authentication but got {}. Will return first one.",
                    getAuthentication().getAuthorities());
        }

         return Role.parse(getAuthentication().getAuthorities().stream().findFirst().get().getAuthority());
    }
}