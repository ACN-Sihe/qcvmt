package com.springMVC.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name = "vesselCol", sequenceName = "vesselCol_seq")
@Table(name = "T_VESSELCOL")
public class VesselCol {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vesselCol")
    @Column(name = "vcid")
    private Integer id;

    @Column(name = "vesselid", length = 10)
    private String vesselid;

    @Column(name = "deck_hold", length = 10)
    private String deck_hold;

    @Column(name = "bay", length = 10)
    private String bay;

    @Column(name = "rowstart", length = 10)
    private String rowStart;

    @Column(name = "rowend", length = 10)
    private String rowEnd;

    @Column(name = "tierstart" , length = 10)
    private String tierStart;

    @Column(name = "tierend" , length = 10)
    private String tierEnd;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVesselid() {
        return vesselid;
    }

    public void setVesselid(String vesselid) {
        this.vesselid = vesselid;
    }

    public String getDeck_hold() {
        return deck_hold;
    }

    public void setDeck_hold(String deck_hold) {
        this.deck_hold = deck_hold;
    }

    public String getBay() {
        return bay;
    }

    public void setBay(String bay) {
        this.bay = bay;
    }

    public String getRowStart() {
        return rowStart;
    }

    public void setRowStart(String rowStart) {
        this.rowStart = rowStart;
    }

    public String getRowEnd() {
        return rowEnd;
    }

    public void setRowEnd(String rowEnd) {
        this.rowEnd = rowEnd;
    }

    public String getTierStart() {
        return tierStart;
    }

    public void setTierStart(String tierStart) {
        this.tierStart = tierStart;
    }

    public String getTierEnd() {
        return tierEnd;
    }

    public void setTierEnd(String tierEnd) {
        this.tierEnd = tierEnd;
    }

    @Override
    public String toString() {
        return "VesselCol{" +
                "id=" + id +
                ", vesselid='" + vesselid + '\'' +
                ", deck_hold='" + deck_hold + '\'' +
                ", bay='" + bay + '\'' +
                ", rowStart='" + rowStart + '\'' +
                ", rowEnd='" + rowEnd + '\'' +
                ", tierStart='" + tierStart + '\'' +
                ", tierEnd='" + tierEnd + '\'' +
                '}';
    }
}
