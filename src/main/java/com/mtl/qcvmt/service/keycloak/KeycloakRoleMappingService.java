package com.mtl.qcvmt.service.keycloak;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KeycloakRoleMappingService {

  private final Keycloak keycloak;
  private final String realmName;

  public KeycloakRoleMappingService(Keycloak keycloak, @Value("${qcvmt.keycloak.realm}") String realmName) {
    this.keycloak = keycloak;
    this.realmName = realmName;
  }

  public String createUserInKeycloak(String username, String password, String role) {
    RealmResource realm = realm();

    UserRepresentation user = new UserRepresentation();
    user.setUsername(username);
    user.setEnabled(true);

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    credential.setTemporary(false);
    user.setCredentials(List.of(credential));

    try (Response response = realm.users().create(user)) {
      if (response.getStatus() != 201) {
        throw new ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "Failed to create user in Keycloak, status=" + response.getStatus());
      }
      String location = response.getLocation().toString();
      String userId = location.substring(location.lastIndexOf('/') + 1);
      assignRole(userId, role);
      return userId;
    }
  }

  public void assignRole(String keycloakUserId, String roleName) {
    UserResource user = realm().users().get(keycloakUserId);
    RoleRepresentation role = realm().roles().get(roleName).toRepresentation();
    user.roles().realmLevel().add(List.of(role));
  }

  public void removeRole(String keycloakUserId, String roleName) {
    UserResource user = realm().users().get(keycloakUserId);
    RoleRepresentation role = realm().roles().get(roleName).toRepresentation();
    user.roles().realmLevel().remove(List.of(role));
  }

  public List<String> getUserRoles(String keycloakUserId) {
    UserResource user = realm().users().get(keycloakUserId);
    return user.roles().realmLevel().listEffective().stream()
        .map(RoleRepresentation::getName)
        .collect(Collectors.toList());
  }

  public RealmRepresentation getRealmRepresentation() {
    return realm().toRepresentation();
  }

  private RealmResource realm() {
    return keycloak.realm(realmName);
  }
}
