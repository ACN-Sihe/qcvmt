package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.cellmatrix.CellMatrixResponse;
import com.mtl.qcvmt.dto.cellmatrix.CreateCellMatrixRequest;
import com.mtl.qcvmt.dto.cellmatrix.UpdateCellMatrixRequest;
import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.service.CellMatrixService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cell-matrix")
@PreAuthorize("hasRole('qcvmt-admin')")
public class CellMatrixController {

  private final CellMatrixService cellMatrixService;

  public CellMatrixController(CellMatrixService cellMatrixService) {
    this.cellMatrixService = cellMatrixService;
  }

  @GetMapping
  public ApiResponse<List<CellMatrixResponse>> list() {
    return ApiResponse.ok(cellMatrixService.list());
  }

  @GetMapping("/{id}")
  public ApiResponse<CellMatrixResponse> get(@PathVariable Integer id) {
    return ApiResponse.ok(cellMatrixService.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CellMatrixResponse> create(@Valid @RequestBody CreateCellMatrixRequest request) {
    CellMatrixResponse created = cellMatrixService.create(request);
    return ApiResponse.ok("Created", created);
  }

  @PutMapping("/{id}")
  public ApiResponse<CellMatrixResponse> update(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateCellMatrixRequest request) {
    CellMatrixResponse updated = cellMatrixService.update(id, request);
    return ApiResponse.ok("Updated", updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Integer id) {
    cellMatrixService.delete(id);
    return ApiResponse.ok("Deleted", null);
  }
}
