package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.user.CreateUserRequest;
import com.mtl.qcvmt.dto.user.UpdateUserRequest;
import com.mtl.qcvmt.dto.user.UserResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserService {

  List<UserResponse> list();

  PageResponse<UserResponse> list(Pageable pageable, String keyword);

  UserResponse get(Integer id);

  UserResponse create(CreateUserRequest request);

  UserResponse update(Integer id, UpdateUserRequest request);

  void delete(Integer id);
}
