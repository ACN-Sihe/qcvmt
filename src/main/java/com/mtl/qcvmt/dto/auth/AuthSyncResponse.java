package com.mtl.qcvmt.dto.auth;

import java.time.Instant;

public record AuthSyncResponse(AuthMeResponse user,boolean created,Instant loginTime){}
