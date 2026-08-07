package com.mtl.qcvmt.service.impl;

import com.mtl.qcvmt.dto.vessel.CreateVesselRequest;
import com.mtl.qcvmt.dto.vessel.UpdateVesselRequest;
import com.mtl.qcvmt.dto.vessel.VesselResponse;
import com.mtl.qcvmt.entity.Vessel;
import com.mtl.qcvmt.repository.VesselRepository;
import com.mtl.qcvmt.service.VesselService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VesselServiceImpl implements VesselService {

  private final VesselRepository vesselRepository;

  public VesselServiceImpl(VesselRepository vesselRepository) {
    this.vesselRepository = vesselRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<VesselResponse> list() {
    return vesselRepository.findAll(Sort.by(Sort.Direction.ASC, "vesselId")).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VesselResponse get(Integer id) {
    Vessel vessel = vesselRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vessel not found"));
    return toResponse(vessel);
  }

  @Override
  @Transactional
  public VesselResponse create(CreateVesselRequest request) {
    if (vesselRepository
        .findByVesselIdAndDeckHoldAndBay(request.vesselId(), request.deckHold(), request.bay())
        .isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Vessel/deckHold/bay already exists");
    }

    Vessel vessel = new Vessel();
    vessel.setVesselId(request.vesselId());
    vessel.setDeckHold(request.deckHold());
    vessel.setBay(request.bay());
    vessel.setRowStart(request.rowStart());
    vessel.setRowEnd(request.rowEnd());
    vessel.setTierStart(request.tierStart());
    vessel.setTierEnd(request.tierEnd());

    return toResponse(vesselRepository.save(vessel));
  }

  @Override
  @Transactional
  public VesselResponse update(Integer id, UpdateVesselRequest request) {
    Vessel vessel = vesselRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vessel not found"));

    vessel.setRowStart(request.rowStart());
    vessel.setRowEnd(request.rowEnd());
    vessel.setTierStart(request.tierStart());
    vessel.setTierEnd(request.tierEnd());
    return toResponse(vesselRepository.save(vessel));
  }

  @Override
  @Transactional
  public void delete(Integer id) {
    if (!vesselRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vessel not found");
    }
    vesselRepository.deleteById(id);
  }

  private VesselResponse toResponse(Vessel vessel) {
    return new VesselResponse(
        vessel.getId(),
        vessel.getVesselId(),
        vessel.getDeckHold(),
        vessel.getBay(),
        vessel.getRowStart(),
        vessel.getRowEnd(),
        vessel.getTierStart(),
        vessel.getTierEnd(),
        vessel.getVersion());
  }
}
