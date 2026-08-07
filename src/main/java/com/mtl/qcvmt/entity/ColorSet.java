package com.mtl.qcvmt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_col_set", uniqueConstraints = @UniqueConstraint(name = "uk_boxcase", columnNames = { "boxcase" }))
public class ColorSet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "boxcase")
  private String boxcase;

  @Column(name = "color")
  private String color;

  @Version
  @Column(name = "version")
  private Integer version;
}
