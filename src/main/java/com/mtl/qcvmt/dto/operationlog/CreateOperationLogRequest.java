package com.mtl.qcvmt.dto.operationlog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateOperationLogRequest(Integer userId,@NotBlank @Size(max=20)String username,@NotBlank @Size(max=50)String functionName,@NotBlank @Size(max=20)String actionType,String oldValues,String newValues,LocalDateTime timestamp){}
