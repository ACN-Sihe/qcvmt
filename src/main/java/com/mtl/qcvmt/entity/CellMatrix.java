package com.mtl.qcvmt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_cell_matrix")
public class CellMatrix {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "type")
  private String type;

  @Column(name = "row")
  private String row;

  @Column(name = "tier")
  private String tier;

  @Column(name = "tier_start")
  private String tierStart;

  @Column(name = "tier_end")
  private String tierEnd;

  @Column(name = "active")
  private String active;
}
