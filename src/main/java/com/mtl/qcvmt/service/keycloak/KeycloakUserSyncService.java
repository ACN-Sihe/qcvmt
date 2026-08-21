package com.mtl.qcvmt.service.keycloak;

import com.mtl.qcvmt.entity.User;
import com.mtl.qcvmt.repository.UserRepository;
import com.mtl.qcvmt.security.SecurityContextHelper;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeycloakUserSyncService {

  private static final Logger log = LoggerFactory.getLogger(KeycloakUserSyncService.class);

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
    if (username == null || username.isBlank()) {
      throw new IllegalStateException("Current username is missing in token");
    }

    java.util.Optional<SyncResult> byKeycloakId = java.util.Optional.empty();
    if (keycloakId != null && !keycloakId.isBlank()) {
      List<User> usersByKeycloakId = userRepository.findAllByKeycloakIdOrderByIdAsc(keycloakId);
      if (!usersByKeycloakId.isEmpty()) {
        if (usersByKeycloakId.size() > 1) {
          log.warn("Duplicate local users found by keycloakId='{}', count={}, using earliest id={}",
              keycloakId, usersByKeycloakId.size(), usersByKeycloakId.get(0).getId());
        }
        byKeycloakId = java.util.Optional.of(new SyncResult(usersByKeycloakId.get(0), false));
      }
    }

    return byKeycloakId.or(() -> {
      List<User> users = userRepository.findAllByUsernameOrderByIdAsc(username);
      if (users.isEmpty()) {
        return java.util.Optional.empty();
      }
      if (users.size() > 1) {
        log.warn("Duplicate local users found by username='{}', count={}, using earliest id={}",
            username, users.size(), users.get(0).getId());
      }

      User user = users.get(0);
      if (keycloakId != null && !keycloakId.isBlank()) {
        user.setKeycloakId(keycloakId);
      }
      if (user.getRole() == null || user.getRole().isBlank()) {
        user.setRole(admin ? "ADMIN" : "USER");
      }
      return java.util.Optional.of(new SyncResult(userRepository.save(user), false));
    })
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
