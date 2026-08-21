package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import com.mtl.qcvmt.dto.operationlog.CreateOperationLogRequest;
import com.mtl.qcvmt.dto.operationlog.OperationLogResponse;
import com.mtl.qcvmt.dto.operationlog.UpdateOperationLogRequest;
import com.mtl.qcvmt.service.OperationLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/operation-logs")
@PreAuthorize("hasRole('qcvmt-admin')")
@Validated
public class OperationLogController {

  private final OperationLogService operationLogService;

  public OperationLogController(OperationLogService operationLogService) {
    this.operationLogService = operationLogService;
  }

  @GetMapping
  public ApiResponse<PageResponse<OperationLogResponse>> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
      @RequestParam(required = false) Integer userId) {
    return ApiResponse.ok(operationLogService.list(
        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")), userId));
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
