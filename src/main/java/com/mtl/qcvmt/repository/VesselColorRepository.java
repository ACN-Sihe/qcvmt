package com.mtl.qcvmt.repository;

import com.mtl.qcvmt.entity.VesselColor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VesselColorRepository extends JpaRepository<VesselColor, Integer> {

  Optional<VesselColor> findByVesselIdAndDeckHoldAndBay(String vesselId, String deckHold, String bay);
}
