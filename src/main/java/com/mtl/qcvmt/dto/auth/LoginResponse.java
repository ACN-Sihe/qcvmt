package com.mtl.qcvmt.dto.auth;

public record LoginResponse(String accessToken,String refreshToken,String tokenType,long expiresIn,long refreshExpiresIn,String scope,AuthMeResponse user){}
