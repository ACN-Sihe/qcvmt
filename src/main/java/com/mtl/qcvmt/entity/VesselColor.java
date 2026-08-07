package com.mtl.qcvmt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_vessel_col")
public class VesselColor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "vesselid")
  private String vesselId;

  @Column(name = "deck_hold")
  private String deckHold;

  @Column(name = "bay")
  private String bay;

  @Column(name = "row_start")
  private String rowStart;

  @Column(name = "row_end")
  private String rowEnd;

  @Column(name = "tier_start")
  private String tierStart;

  @Column(name = "tier_end")
  private String tierEnd;

  @Version
  @Column(name = "version")
  private Integer version;
}
