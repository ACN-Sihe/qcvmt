package com.mtl.qcvmt.repository;

import com.mtl.qcvmt.entity.ShowLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowLogRepository extends JpaRepository<ShowLog, Integer> {

  List<ShowLog> findByLoginTimeBetweenOrderByLoginTimeDesc(LocalDateTime from, LocalDateTime to);
}
