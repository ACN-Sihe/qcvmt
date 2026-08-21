package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.vesselcolor.CreateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.UpdateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.VesselColorResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface VesselColorService {

  List<VesselColorResponse> list();

  PageResponse<VesselColorResponse> list(Pageable pageable, String keyword);

  VesselColorResponse get(Integer id);

  VesselColorResponse create(CreateVesselColorRequest request);

  VesselColorResponse update(Integer id, UpdateVesselColorRequest request);

  void delete(Integer id);
}
