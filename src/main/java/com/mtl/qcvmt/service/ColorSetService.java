package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.colorset.ColorSetResponse;
import com.mtl.qcvmt.dto.colorset.CreateColorSetRequest;
import com.mtl.qcvmt.dto.colorset.UpdateColorSetRequest;
import com.mtl.qcvmt.dto.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ColorSetService {

  List<ColorSetResponse> list();

  PageResponse<ColorSetResponse> list(Pageable pageable, String keyword);

  ColorSetResponse get(Integer id);

  ColorSetResponse create(CreateColorSetRequest request);

  ColorSetResponse update(Integer id, UpdateColorSetRequest request);

  void delete(Integer id);
}
