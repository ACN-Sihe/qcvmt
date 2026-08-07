package com.mtl.qcvmt.repository;

import com.mtl.qcvmt.entity.Vessel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VesselRepository extends JpaRepository<Vessel, Integer> {

  Optional<Vessel> findByVesselIdAndDeckHoldAndBay(String vesselId, String deckHold, String bay);
}
