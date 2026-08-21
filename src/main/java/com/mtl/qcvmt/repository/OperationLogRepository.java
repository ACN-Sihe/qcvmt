package com.mtl.qcvmt.repository;

import com.mtl.qcvmt.entity.OperationLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Integer> {

  List<OperationLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);

  Page<OperationLog> findByUserId(Integer userId, Pageable pageable);
}
