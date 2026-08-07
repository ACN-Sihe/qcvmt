package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.user.CreateUserRequest;
import com.mtl.qcvmt.dto.user.UpdateUserRequest;
import com.mtl.qcvmt.dto.user.UserResponse;
import com.mtl.qcvmt.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('qcvmt-admin')")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<List<UserResponse>> list() {
    return ApiResponse.ok(userService.list());
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
