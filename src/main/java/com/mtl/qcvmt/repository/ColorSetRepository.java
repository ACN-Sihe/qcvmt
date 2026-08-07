package com.mtl.qcvmt.repository;

import com.mtl.qcvmt.entity.ColorSet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorSetRepository extends JpaRepository<ColorSet, Integer> {

  Optional<ColorSet> findByBoxcase(String boxcase);
}
