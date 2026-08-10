package com.mtl.qcvmt.dto.auth;

import java.time.Instant;
import java.util.List;

public record AuthMeResponse(Integer id,String keycloakId,String username,String qcid,String localRole,List<String>roles,boolean admin,Instant tokenExpiresAt){}
