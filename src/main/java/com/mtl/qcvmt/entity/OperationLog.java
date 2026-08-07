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
@Table(name = "t_operation_log")
public class OperationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "userid")
  private Integer userId;

  @Column(name = "username")
  private String username;

  @Column(name = "function_name")
  private String functionName;

  @Column(name = "action_type")
  private String actionType;

  @Column(name = "old_values")
  private String oldValues;

  @Column(name = "new_values")
  private String newValues;

  @Column(name = "timestamp")
  private LocalDateTime timestamp;
}
