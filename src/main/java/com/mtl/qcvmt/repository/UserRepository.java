package com.mtl.qcvmt.repository;

import com.mtl.qcvmt.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByUsername(String username);

  Optional<User> findByKeycloakId(String keycloakId);
}
