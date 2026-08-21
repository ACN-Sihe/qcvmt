package com.mtl.qcvmt.dto.auth;

import java.util.List;

public record AuthPermissionsResponse(List<String>roles,List<String>permissions,boolean admin){}
