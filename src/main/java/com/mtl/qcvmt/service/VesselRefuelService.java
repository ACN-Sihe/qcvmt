package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.vesselrefuel.CreateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.UpdateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.VesselRefuelResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface VesselRefuelService {

  List<VesselRefuelResponse> list();

  PageResponse<VesselRefuelResponse> list(Pageable pageable, String keyword);

  VesselRefuelResponse get(Integer id);

  VesselRefuelResponse create(CreateVesselRefuelRequest request);

  VesselRefuelResponse update(Integer id, UpdateVesselRefuelRequest request);

  void delete(Integer id);
}
