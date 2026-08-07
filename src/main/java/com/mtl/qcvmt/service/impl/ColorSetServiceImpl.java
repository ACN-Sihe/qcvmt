package com.mtl.qcvmt.service.impl;

import com.mtl.qcvmt.dto.colorset.ColorSetResponse;
import com.mtl.qcvmt.dto.colorset.CreateColorSetRequest;
import com.mtl.qcvmt.dto.colorset.UpdateColorSetRequest;
import com.mtl.qcvmt.entity.ColorSet;
import com.mtl.qcvmt.repository.ColorSetRepository;
import com.mtl.qcvmt.service.ColorSetService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ColorSetServiceImpl implements ColorSetService {

  private final ColorSetRepository colorSetRepository;

  public ColorSetServiceImpl(ColorSetRepository colorSetRepository) {
    this.colorSetRepository = colorSetRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ColorSetResponse> list() {
    return colorSetRepository.findAll(Sort.by(Sort.Direction.ASC, "boxcase")).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ColorSetResponse get(Integer id) {
    ColorSet colorSet = colorSetRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ColorSet not found"));
    return toResponse(colorSet);
  }

  @Override
  @Transactional
  public ColorSetResponse create(CreateColorSetRequest request) {
    if (colorSetRepository.findByBoxcase(request.boxcase()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Boxcase already exists");
    }

    ColorSet colorSet = new ColorSet();
    colorSet.setBoxcase(request.boxcase());
    colorSet.setColor(request.color());
    return toResponse(colorSetRepository.save(colorSet));
  }

  @Override
  @Transactional
  public ColorSetResponse update(Integer id, UpdateColorSetRequest request) {
    ColorSet colorSet = colorSetRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ColorSet not found"));

    colorSet.setColor(request.color());
    return toResponse(colorSetRepository.save(colorSet));
  }

  @Override
  @Transactional
  public void delete(Integer id) {
    if (!colorSetRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ColorSet not found");
    }
    colorSetRepository.deleteById(id);
  }

  private ColorSetResponse toResponse(ColorSet colorSet) {
    return new ColorSetResponse(
        colorSet.getId(),
        colorSet.getBoxcase(),
        colorSet.getColor(),
        colorSet.getVersion());
  }
}
