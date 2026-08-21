package com.mtl.qcvmt.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextHelper {

  public String getCurrentUsername() {
    Jwt jwt = getCurrentJwt();
    String username = jwt.getClaimAsString("preferred_username");
    return username == null || username.isBlank() ? jwt.getSubject() : username;
  }

  public String getKeycloakSub() {
    return getCurrentJwt().getSubject();
  }

  public boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> "ROLE_qcvmt-admin".equals(authority.getAuthority()));
  }

  private Jwt getCurrentJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
      throw new IllegalStateException("No JWT principal found in security context");
    }
    return jwt;
  }
}
