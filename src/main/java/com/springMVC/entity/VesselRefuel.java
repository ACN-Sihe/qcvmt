package com.springMVC.entity;

import javax.persistence.*;

@Entity
@SequenceGenerator(name="vesselRefuel",sequenceName="vesselRefuel_seq")
@Table(name = "T_VesselRefuel")
public class VesselRefuel {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="vesselRefuel")
    @Column(name = "vrid")
    private Integer id;

    @Column(name = "vesselid" , length = 10)
    private String vesselid;

    @Column(name = "is_refuel" , length = 5)
    private String is_refuel;

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

    public String getIs_refuel() {
        return is_refuel;
    }

    public void setIs_refuel(String is_refuel) {
        this.is_refuel = is_refuel;
    }

    @Override
    public String toString() {
        return "VesselRefuel{" +
                "id=" + id +
                ", vesselid='" + vesselid + '\'' +
                ", is_refuel='" + is_refuel + '\'' +
                '}';
    }
}
