package com.mtl.qcvmt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "keycloak_id")
  private String keycloakId;

  @Column(name = "qcid")
  private String qcid;

  @Column(name = "name")
  private String username;

  @Column(name = "role")
  private String role;

  @Column(name = "parent")
  private String parent;

  @Column(name = "createtime")
  private LocalDateTime createTime;
}
