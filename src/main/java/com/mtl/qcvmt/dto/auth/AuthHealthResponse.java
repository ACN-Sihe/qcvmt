package com.mtl.qcvmt.dto.auth;

import java.time.Instant;
import java.util.List;

public record AuthHealthResponse(boolean authenticated,String subject,String username,String issuer,Instant issuedAt,Instant expiresAt,List<String>roles){}
