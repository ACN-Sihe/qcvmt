package com.mtl.qcvmt.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void testUserCreation() {
    User user = new User();
    user.setId(1);
    user.setKeycloakId("test-keycloak-id");
    user.setUsername("tester");
    user.setQcid("QC01");
    user.setRole("qcvmt-user");
    user.setCreateTime(LocalDateTime.now());

    assertThat(user.getUsername()).isEqualTo("tester");
    assertThat(user.getKeycloakId()).isEqualTo("test-keycloak-id");
  }
}
