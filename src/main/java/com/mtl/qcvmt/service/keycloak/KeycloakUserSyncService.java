package com.mtl.qcvmt.service.keycloak;

import com.mtl.qcvmt.entity.User;
import com.mtl.qcvmt.repository.UserRepository;
import com.mtl.qcvmt.security.SecurityContextHelper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeycloakUserSyncService {

  public record SyncResult(User user, boolean created) {
  }

  private final UserRepository userRepository;
  private final SecurityContextHelper securityContextHelper;

  public KeycloakUserSyncService(UserRepository userRepository, SecurityContextHelper securityContextHelper) {
    this.userRepository = userRepository;
    this.securityContextHelper = securityContextHelper;
  }

  @Transactional
  public User getOrCreateLocalUser() {
    return syncCurrentUser().user();
  }

  @Transactional
  public SyncResult syncCurrentUser() {
    String keycloakId = securityContextHelper.getKeycloakSub();
    String username = securityContextHelper.getCurrentUsername();
    return syncByIdentity(keycloakId, username, securityContextHelper.isAdmin());
  }

  @Transactional
  public SyncResult syncByIdentity(String keycloakId, String username, boolean admin) {
    return userRepository.findByKeycloakId(keycloakId)
        .map(user -> new SyncResult(user, false))
        .or(() -> userRepository.findByUsername(username).map(user -> {
          user.setKeycloakId(keycloakId);
          if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(admin ? "ADMIN" : "USER");
          }
          return new SyncResult(userRepository.save(user), false);
        }))
        .orElseGet(() -> {
          User user = new User();
          user.setKeycloakId(keycloakId);
          user.setUsername(username);
          user.setRole(admin ? "ADMIN" : "USER");
          user.setCreateTime(LocalDateTime.now());
          return new SyncResult(userRepository.save(user), true);
        });
  }
}
