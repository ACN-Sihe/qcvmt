package com.mtl.qcvmt.dto.user;

import java.time.LocalDateTime;

public record UserResponse(Integer id,String keycloakId,String username,String qcid,String role,String parent,LocalDateTime createTime){}
