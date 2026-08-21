package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.operationlog.CreateOperationLogRequest;
import com.mtl.qcvmt.dto.operationlog.OperationLogResponse;
import com.mtl.qcvmt.dto.operationlog.UpdateOperationLogRequest;
import com.mtl.qcvmt.dto.common.PageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OperationLogService {

  List<OperationLogResponse> list();

  PageResponse<OperationLogResponse> list(Pageable pageable, Integer userId);

  List<OperationLogResponse> listByPeriod(LocalDateTime from, LocalDateTime to);

  OperationLogResponse get(Integer id);

  OperationLogResponse create(CreateOperationLogRequest request);

  OperationLogResponse update(Integer id, UpdateOperationLogRequest request);

  void delete(Integer id);
}
