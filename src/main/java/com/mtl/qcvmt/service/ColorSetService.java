package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.colorset.ColorSetResponse;
import com.mtl.qcvmt.dto.colorset.CreateColorSetRequest;
import com.mtl.qcvmt.dto.colorset.UpdateColorSetRequest;
import java.util.List;

public interface ColorSetService {

  List<ColorSetResponse> list();

  ColorSetResponse get(Integer id);

  ColorSetResponse create(CreateColorSetRequest request);

  ColorSetResponse update(Integer id, UpdateColorSetRequest request);

  void delete(Integer id);
}
