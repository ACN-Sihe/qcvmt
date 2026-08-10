package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.auth.AuthHealthResponse;
import com.mtl.qcvmt.dto.auth.AuthLogoutUrlResponse;
import com.mtl.qcvmt.dto.auth.AuthMeResponse;
import com.mtl.qcvmt.dto.auth.AuthPermissionsResponse;
import com.mtl.qcvmt.dto.auth.AuthSyncResponse;
import com.mtl.qcvmt.dto.auth.LoginRequest;
import com.mtl.qcvmt.dto.auth.LoginResponse;
import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @PreAuthorize("permitAll()")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.ok("Login success", authService.login(request));
  }

  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('qcvmt-admin', 'qcvmt-user')")
  public ApiResponse<AuthMeResponse> me() {
    return ApiResponse.ok(authService.me());
  }

  @GetMapping("/permissions")
  @PreAuthorize("hasAnyRole('qcvmt-admin', 'qcvmt-user')")
  public ApiResponse<AuthPermissionsResponse> permissions() {
    return ApiResponse.ok(authService.permissions());
  }

  @PostMapping("/sync")
  @PreAuthorize("hasAnyRole('qcvmt-admin', 'qcvmt-user')")
  public ApiResponse<AuthSyncResponse> sync() {
    return ApiResponse.ok("Synced", authService.sync());
  }

  @GetMapping("/logout-url")
  @PreAuthorize("hasAnyRole('qcvmt-admin', 'qcvmt-user')")
  public ApiResponse<AuthLogoutUrlResponse> logoutUrl(
      @RequestParam(value = "postLogoutRedirectUri", required = false) String postLogoutRedirectUri) {
    return ApiResponse.ok(authService.logoutUrl(postLogoutRedirectUri));
  }

  @GetMapping("/health")
  @PreAuthorize("hasAnyRole('qcvmt-admin', 'qcvmt-user')")
  public ApiResponse<AuthHealthResponse> health() {
    return ApiResponse.ok(authService.health());
  }
}
