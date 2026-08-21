package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.cellmatrix.CellMatrixResponse;
import com.mtl.qcvmt.dto.cellmatrix.UpdateCellMatrixRequest;
import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.request.UpdateBaySizeRequest;
import com.mtl.qcvmt.service.CellMatrixService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bay-config")
@PreAuthorize("hasRole('qcvmt-admin')")
public class BayConfigController {

  private final CellMatrixService cellMatrixService;

  public BayConfigController(CellMatrixService cellMatrixService) {
    this.cellMatrixService = cellMatrixService;
  }

  @GetMapping
  public ApiResponse<List<CellMatrixResponse>> getBayConfig() {
    return ApiResponse.ok(cellMatrixService.list());
  }

  @PutMapping
  public ApiResponse<CellMatrixResponse> updateBaySize(@Valid @RequestBody UpdateBaySizeRequest request) {
    UpdateCellMatrixRequest updateRequest = new UpdateCellMatrixRequest(
        request.type(),
        request.rowStart(),
        request.rowEnd(),
        request.tierStart(),
        request.tierEnd(),
        request.active());
    CellMatrixResponse updated = cellMatrixService.update(request.id(), updateRequest);
    return ApiResponse.ok("Updated", updated);
  }
}
