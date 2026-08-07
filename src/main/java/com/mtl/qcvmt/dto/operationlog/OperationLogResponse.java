package com.mtl.qcvmt.dto.operationlog;

import java.time.LocalDateTime;

public record OperationLogResponse(Integer id,Integer userId,String username,String functionName,String actionType,String oldValues,String newValues,LocalDateTime timestamp){}
