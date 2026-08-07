package com.mtl.qcvmt.service.keycloak;

import com.mtl.qcvmt.entity.User;
import com.mtl.qcvmt.repository.UserRepository;
import com.mtl.qcvmt.security.SecurityContextHelper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeycloakUserSyncService {

  private final UserRepository userRepository;
  private final SecurityContextHelper securityContextHelper;

  public KeycloakUserSyncService(UserRepository userRepository, SecurityContextHelper securityContextHelper) {
    this.userRepository = userRepository;
    this.securityContextHelper = securityContextHelper;
  }

  @Transactional
  public User getOrCreateLocalUser() {
    String keycloakId = securityContextHelper.getKeycloakSub();
    String username = securityContextHelper.getCurrentUsername();

    return userRepository.findByKeycloakId(keycloakId)
        .orElseGet(() -> {
          User user = new User();
          user.setKeycloakId(keycloakId);
          user.setUsername(username);
          user.setRole(securityContextHelper.isAdmin() ? "ADMIN" : "USER");
          user.setCreateTime(LocalDateTime.now());
          return userRepository.save(user);
        });
  }
}
