package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.vessel.CreateVesselRequest;
import com.mtl.qcvmt.dto.vessel.UpdateVesselRequest;
import com.mtl.qcvmt.dto.vessel.VesselResponse;
import java.util.List;

public interface VesselService {

  List<VesselResponse> list();

  VesselResponse get(Integer id);

  VesselResponse create(CreateVesselRequest request);

  VesselResponse update(Integer id, UpdateVesselRequest request);

  void delete(Integer id);
}
