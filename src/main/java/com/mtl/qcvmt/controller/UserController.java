package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import com.mtl.qcvmt.dto.user.CreateUserRequest;
import com.mtl.qcvmt.dto.user.UpdateUserRequest;
import com.mtl.qcvmt.dto.user.UserResponse;
import com.mtl.qcvmt.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('qcvmt-admin')")
@Validated
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<PageResponse<UserResponse>> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(userService.list(PageRequest.of(page, size, Sort.by("username")), keyword));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserResponse> get(@PathVariable Integer id) {
    return ApiResponse.ok(userService.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    UserResponse created = userService.create(request);
    return ApiResponse.ok("Created", created);
  }

  @PutMapping("/{id}")
  public ApiResponse<UserResponse> update(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateUserRequest request) {
    UserResponse updated = userService.update(id, request);
    return ApiResponse.ok("Updated", updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Integer id) {
    userService.delete(id);
    return ApiResponse.ok("Deleted", null);
  }
}
