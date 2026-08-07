package com.mtl.qcvmt.service.impl;

import com.mtl.qcvmt.dto.vesselcolor.CreateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.UpdateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.VesselColorResponse;
import com.mtl.qcvmt.entity.VesselColor;
import com.mtl.qcvmt.repository.VesselColorRepository;
import com.mtl.qcvmt.service.VesselColorService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VesselColorServiceImpl implements VesselColorService {

  private final VesselColorRepository vesselColorRepository;

  public VesselColorServiceImpl(VesselColorRepository vesselColorRepository) {
    this.vesselColorRepository = vesselColorRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<VesselColorResponse> list() {
    return vesselColorRepository.findAll(Sort.by(Sort.Direction.ASC, "vesselId")).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VesselColorResponse get(Integer id) {
    VesselColor entity = vesselColorRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VesselColor not found"));
    return toResponse(entity);
  }

  @Override
  @Transactional
  public VesselColorResponse create(CreateVesselColorRequest request) {
    if (vesselColorRepository
        .findByVesselIdAndDeckHoldAndBay(request.vesselId(), request.deckHold(), request.bay())
        .isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "VesselColor vessel/deckHold/bay already exists");
    }

    VesselColor entity = new VesselColor();
    entity.setVesselId(request.vesselId());
    entity.setDeckHold(request.deckHold());
    entity.setBay(request.bay());
    entity.setRowStart(request.rowStart());
    entity.setRowEnd(request.rowEnd());
    entity.setTierStart(request.tierStart());
    entity.setTierEnd(request.tierEnd());
    return toResponse(vesselColorRepository.save(entity));
  }

  @Override
  @Transactional
  public VesselColorResponse update(Integer id, UpdateVesselColorRequest request) {
    VesselColor entity = vesselColorRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VesselColor not found"));

    entity.setRowStart(request.rowStart());
    entity.setRowEnd(request.rowEnd());
    entity.setTierStart(request.tierStart());
    entity.setTierEnd(request.tierEnd());
    return toResponse(vesselColorRepository.save(entity));
  }

  @Override
  @Transactional
  public void delete(Integer id) {
    if (!vesselColorRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "VesselColor not found");
    }
    vesselColorRepository.deleteById(id);
  }

  private VesselColorResponse toResponse(VesselColor entity) {
    return new VesselColorResponse(
        entity.getId(),
        entity.getVesselId(),
        entity.getDeckHold(),
        entity.getBay(),
        entity.getRowStart(),
        entity.getRowEnd(),
        entity.getTierStart(),
        entity.getTierEnd(),
        entity.getVersion());
  }
}
