package com.mtl.qcvmt.service.impl;

import com.mtl.qcvmt.dto.operationlog.CreateOperationLogRequest;
import com.mtl.qcvmt.dto.operationlog.OperationLogResponse;
import com.mtl.qcvmt.dto.operationlog.UpdateOperationLogRequest;
import com.mtl.qcvmt.dto.common.PageResponse;
import com.mtl.qcvmt.entity.OperationLog;
import com.mtl.qcvmt.repository.OperationLogRepository;
import com.mtl.qcvmt.service.OperationLogService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OperationLogServiceImpl implements OperationLogService {

  private final OperationLogRepository operationLogRepository;

  public OperationLogServiceImpl(OperationLogRepository operationLogRepository) {
    this.operationLogRepository = operationLogRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<OperationLogResponse> list() {
    return operationLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<OperationLogResponse> list(Pageable pageable, Integer userId) {
    Page<OperationLog> logs = userId == null
        ? operationLogRepository.findAll(pageable)
        : operationLogRepository.findByUserId(userId, pageable);
    return PageResponse.from(logs.map(this::toResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public List<OperationLogResponse> listByPeriod(LocalDateTime from, LocalDateTime to) {
    return operationLogRepository.findByTimestampBetweenOrderByTimestampDesc(from, to).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public OperationLogResponse get(Integer id) {
    OperationLog entity = operationLogRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OperationLog not found"));
    return toResponse(entity);
  }

  @Override
  @Transactional
  public OperationLogResponse create(CreateOperationLogRequest request) {
    OperationLog entity = new OperationLog();
    entity.setUserId(request.userId());
    entity.setUsername(request.username());
    entity.setFunctionName(request.functionName());
    entity.setActionType(request.actionType());
    entity.setOldValues(request.oldValues());
    entity.setNewValues(request.newValues());
    entity.setTimestamp(request.timestamp() == null ? LocalDateTime.now() : request.timestamp());
    return toResponse(operationLogRepository.save(entity));
  }

  @Override
  @Transactional
  public OperationLogResponse update(Integer id, UpdateOperationLogRequest request) {
    OperationLog entity = operationLogRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OperationLog not found"));

    entity.setUserId(request.userId());
    entity.setUsername(request.username());
    entity.setFunctionName(request.functionName());
    entity.setActionType(request.actionType());
    entity.setOldValues(request.oldValues());
    entity.setNewValues(request.newValues());
    entity.setTimestamp(request.timestamp());
    return toResponse(operationLogRepository.save(entity));
  }

  @Override
  @Transactional
  public void delete(Integer id) {
    if (!operationLogRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "OperationLog not found");
    }
    operationLogRepository.deleteById(id);
  }

  private OperationLogResponse toResponse(OperationLog entity) {
    return new OperationLogResponse(
        entity.getId(),
        entity.getUserId(),
        entity.getUsername(),
        entity.getFunctionName(),
        entity.getActionType(),
        entity.getOldValues(),
        entity.getNewValues(),
        entity.getTimestamp());
  }
}
