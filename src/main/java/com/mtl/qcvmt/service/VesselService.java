package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.vessel.CreateVesselRequest;
import com.mtl.qcvmt.dto.vessel.UpdateVesselRequest;
import com.mtl.qcvmt.dto.vessel.VesselResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface VesselService {

  List<VesselResponse> list();

  PageResponse<VesselResponse> list(Pageable pageable, String keyword);

  List<VesselResponse> listByVesselId(String vesselId);

  VesselResponse get(Integer id);

  VesselResponse create(CreateVesselRequest request);

  VesselResponse update(Integer id, UpdateVesselRequest request);

  void delete(Integer id);
}
