package com.mtl.qcvmt.service.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mtl.qcvmt.dto.user.UserResponse;
import com.mtl.qcvmt.entity.User;
import com.mtl.qcvmt.repository.UserRepository;
import com.mtl.qcvmt.service.impl.UserServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  void testFindAll() {
    User user = new User(1, "kid-1", "QC83", "alice", "qcvmt-user", "admin", LocalDateTime.now());
    when(userRepository.findAll(
        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "username")))
        .thenReturn(List.of(user));

    List<UserResponse> result = userService.list();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).username()).isEqualTo("alice");
  }
}
