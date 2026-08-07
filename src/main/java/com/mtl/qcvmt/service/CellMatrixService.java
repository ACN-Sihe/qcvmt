package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.cellmatrix.CellMatrixResponse;
import com.mtl.qcvmt.dto.cellmatrix.CreateCellMatrixRequest;
import com.mtl.qcvmt.dto.cellmatrix.UpdateCellMatrixRequest;
import java.util.List;

public interface CellMatrixService {

  List<CellMatrixResponse> list();

  CellMatrixResponse get(Integer id);

  CellMatrixResponse create(CreateCellMatrixRequest request);

  CellMatrixResponse update(Integer id, UpdateCellMatrixRequest request);

  void delete(Integer id);
}
