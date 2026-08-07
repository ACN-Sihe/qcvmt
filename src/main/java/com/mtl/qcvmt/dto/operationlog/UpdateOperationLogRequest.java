package com.mtl.qcvmt.dto.operationlog;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateOperationLogRequest(Integer userId,@Size(max=20)String username,@Size(max=50)String functionName,@Size(max=20)String actionType,String oldValues,String newValues,LocalDateTime timestamp){}
