package com.mtl.qcvmt.service.impl;

import com.mtl.qcvmt.dto.user.CreateUserRequest;
import com.mtl.qcvmt.dto.user.UpdateUserRequest;
import com.mtl.qcvmt.dto.user.UserResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import com.mtl.qcvmt.entity.User;
import com.mtl.qcvmt.repository.UserRepository;
import com.mtl.qcvmt.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> list() {
    return userRepository.findAll(Sort.by(Sort.Direction.ASC, "username")).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<UserResponse> list(Pageable pageable, String keyword) {
    Page<User> users = hasKeyword(keyword)
        ? userRepository.findAll(Example.of(userSearchProbe(keyword), keywordMatcher()), pageable)
        : userRepository.findAll(pageable);
    return PageResponse.from(users.map(this::toResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse get(Integer id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return toResponse(user);
  }

  @Override
  @Transactional
  public UserResponse create(CreateUserRequest request) {
    if (!userRepository.findAllByUsernameOrderByIdAsc(request.username()).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }

    User user = new User();
    user.setUsername(request.username());
    user.setQcid(request.qcid());
    user.setRole(request.role());
    user.setParent(request.parent());
    user.setCreateTime(LocalDateTime.now());

    return toResponse(userRepository.save(user));
  }

  @Override
  @Transactional
  public UserResponse update(Integer id, UpdateUserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    user.setQcid(request.qcid());
    user.setRole(request.role());
    user.setParent(request.parent());
    return toResponse(userRepository.save(user));
  }

  @Override
  @Transactional
  public void delete(Integer id) {
    if (!userRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    userRepository.deleteById(id);
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getKeycloakId(),
        user.getUsername(),
        user.getQcid(),
        user.getRole(),
        user.getParent(),
        user.getCreateTime());
  }

  private User userSearchProbe(String keyword) {
    User probe = new User();
    probe.setUsername(keyword);
    probe.setQcid(keyword);
    probe.setRole(keyword);
    probe.setParent(keyword);
    return probe;
  }

  private boolean hasKeyword(String keyword) {
    return keyword != null && !keyword.isBlank();
  }

  private ExampleMatcher keywordMatcher() {
    return ExampleMatcher.matchingAny()
        .withIgnoreCase()
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
  }
}
