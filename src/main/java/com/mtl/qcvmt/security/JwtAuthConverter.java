package com.mtl.qcvmt.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String ROLE_PREFIX = "qcvmt-";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractRoles(jwt);
    String principalName = jwt.getClaimAsString("preferred_username");
    if (principalName == null || principalName.isBlank()) {
      principalName = jwt.getSubject();
    }
    return new JwtAuthenticationToken(jwt, authorities, principalName);
  }

  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    Object realmAccess = jwt.getClaims().get("realm_access");
    if (!(realmAccess instanceof Map<?, ?> realmMap)) {
      return authorities;
    }

    Object roles = realmMap.get("roles");
    if (!(roles instanceof Collection<?> roleCollection)) {
      return authorities;
    }

    for (Object roleObj : roleCollection) {
      if (!(roleObj instanceof String role) || !role.startsWith(ROLE_PREFIX)) {
        continue;
      }
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
    }
    return authorities;
  }
}
