package com.mtl.qcvmt.service.impl;

import com.mtl.qcvmt.dto.vesselrefuel.CreateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.UpdateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.VesselRefuelResponse;
import com.mtl.qcvmt.entity.VesselRefuel;
import com.mtl.qcvmt.repository.VesselRefuelRepository;
import com.mtl.qcvmt.service.VesselRefuelService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VesselRefuelServiceImpl implements VesselRefuelService {

  private final VesselRefuelRepository vesselRefuelRepository;

  public VesselRefuelServiceImpl(VesselRefuelRepository vesselRefuelRepository) {
    this.vesselRefuelRepository = vesselRefuelRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<VesselRefuelResponse> list() {
    return vesselRefuelRepository.findAll(Sort.by(Sort.Direction.ASC, "vesselId")).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VesselRefuelResponse get(Integer id) {
    VesselRefuel entity = vesselRefuelRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VesselRefuel not found"));
    return toResponse(entity);
  }

  @Override
  @Transactional
  public VesselRefuelResponse create(CreateVesselRefuelRequest request) {
    if (vesselRefuelRepository.findByVesselId(request.vesselId()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "VesselRefuel for vessel already exists");
    }

    VesselRefuel entity = new VesselRefuel();
    entity.setVesselId(request.vesselId());
    entity.setIsRefuel(request.isRefuel());
    return toResponse(vesselRefuelRepository.save(entity));
  }

  @Override
  @Transactional
  public VesselRefuelResponse update(Integer id, UpdateVesselRefuelRequest request) {
    VesselRefuel entity = vesselRefuelRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VesselRefuel not found"));

    entity.setIsRefuel(request.isRefuel());
    return toResponse(vesselRefuelRepository.save(entity));
  }

  @Override
  @Transactional
  public void delete(Integer id) {
    if (!vesselRefuelRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "VesselRefuel not found");
    }
    vesselRefuelRepository.deleteById(id);
  }

  private VesselRefuelResponse toResponse(VesselRefuel entity) {
    return new VesselRefuelResponse(
        entity.getId(),
        entity.getVesselId(),
        entity.getIsRefuel(),
        entity.getVersion());
  }
}
