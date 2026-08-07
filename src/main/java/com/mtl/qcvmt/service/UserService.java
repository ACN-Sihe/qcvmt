package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.user.CreateUserRequest;
import com.mtl.qcvmt.dto.user.UpdateUserRequest;
import com.mtl.qcvmt.dto.user.UserResponse;
import java.util.List;

public interface UserService {

  List<UserResponse> list();

  UserResponse get(Integer id);

  UserResponse create(CreateUserRequest request);

  UserResponse update(Integer id, UpdateUserRequest request);

  void delete(Integer id);
}
