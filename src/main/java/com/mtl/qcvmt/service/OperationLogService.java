package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.operationlog.CreateOperationLogRequest;
import com.mtl.qcvmt.dto.operationlog.OperationLogResponse;
import com.mtl.qcvmt.dto.operationlog.UpdateOperationLogRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogService {

  List<OperationLogResponse> list();

  List<OperationLogResponse> listByPeriod(LocalDateTime from, LocalDateTime to);

  OperationLogResponse get(Integer id);

  OperationLogResponse create(CreateOperationLogRequest request);

  OperationLogResponse update(Integer id, UpdateOperationLogRequest request);

  void delete(Integer id);
}
