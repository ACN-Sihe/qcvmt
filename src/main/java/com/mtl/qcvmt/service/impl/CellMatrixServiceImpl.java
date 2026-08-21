package com.mtl.qcvmt.service.impl;

import com.mtl.qcvmt.dto.cellmatrix.CellMatrixResponse;
import com.mtl.qcvmt.dto.cellmatrix.CreateCellMatrixRequest;
import com.mtl.qcvmt.dto.cellmatrix.UpdateCellMatrixRequest;
import com.mtl.qcvmt.entity.CellMatrix;
import com.mtl.qcvmt.repository.CellMatrixRepository;
import com.mtl.qcvmt.service.CellMatrixService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CellMatrixServiceImpl implements CellMatrixService {

  private final CellMatrixRepository cellMatrixRepository;

  public CellMatrixServiceImpl(CellMatrixRepository cellMatrixRepository) {
    this.cellMatrixRepository = cellMatrixRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CellMatrixResponse> list() {
    return cellMatrixRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public CellMatrixResponse get(Integer id) {
    CellMatrix entity = cellMatrixRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CellMatrix not found"));
    return toResponse(entity);
  }

  @Override
  @Transactional
  public CellMatrixResponse create(CreateCellMatrixRequest request) {
    CellMatrix entity = new CellMatrix();
    entity.setType(request.type());
    entity.setRow(request.row());
    entity.setTier(request.tier());
    entity.setTierStart(request.tierStart());
    entity.setTierEnd(request.tierEnd());
    entity.setActive(request.active());
    return toResponse(cellMatrixRepository.save(entity));
  }

  @Override
  @Transactional
  public CellMatrixResponse update(Integer id, UpdateCellMatrixRequest request) {
    CellMatrix entity = cellMatrixRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CellMatrix not found"));

    entity.setType(request.type());
    entity.setRow(request.row());
    entity.setTier(request.tier());
    entity.setTierStart(request.tierStart());
    entity.setTierEnd(request.tierEnd());
    entity.setActive(request.active());
    return toResponse(cellMatrixRepository.save(entity));
  }

  @Override
  @Transactional
  public void delete(Integer id) {
    if (!cellMatrixRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CellMatrix not found");
    }
    cellMatrixRepository.deleteById(id);
  }

  private CellMatrixResponse toResponse(CellMatrix entity) {
    return new CellMatrixResponse(
        entity.getId(),
        entity.getType(),
        entity.getRow(),
        entity.getTier(),
        entity.getTierStart(),
        entity.getTierEnd(),
        entity.getActive());
  }
}
