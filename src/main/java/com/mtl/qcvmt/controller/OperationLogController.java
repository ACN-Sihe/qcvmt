package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.operationlog.CreateOperationLogRequest;
import com.mtl.qcvmt.dto.operationlog.OperationLogResponse;
import com.mtl.qcvmt.dto.operationlog.UpdateOperationLogRequest;
import com.mtl.qcvmt.service.OperationLogService;
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
@RequestMapping("/api/operation-logs")
@PreAuthorize("hasRole('qcvmt-admin')")
public class OperationLogController {

  private final OperationLogService operationLogService;

  public OperationLogController(OperationLogService operationLogService) {
    this.operationLogService = operationLogService;
  }

  @GetMapping
  public ApiResponse<List<OperationLogResponse>> list() {
    return ApiResponse.ok(operationLogService.list());
  }

  @GetMapping("/{id}")
  public ApiResponse<OperationLogResponse> get(@PathVariable Integer id) {
    return ApiResponse.ok(operationLogService.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<OperationLogResponse> create(@Valid @RequestBody CreateOperationLogRequest request) {
    OperationLogResponse created = operationLogService.create(request);
    return ApiResponse.ok("Created", created);
  }

  @PutMapping("/{id}")
  public ApiResponse<OperationLogResponse> update(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateOperationLogRequest request) {
    OperationLogResponse updated = operationLogService.update(id, request);
    return ApiResponse.ok("Updated", updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Integer id) {
    operationLogService.delete(id);
    return ApiResponse.ok("Deleted", null);
  }
}
