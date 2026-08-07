package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.vesselrefuel.CreateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.UpdateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.VesselRefuelResponse;
import java.util.List;

public interface VesselRefuelService {

  List<VesselRefuelResponse> list();

  VesselRefuelResponse get(Integer id);

  VesselRefuelResponse create(CreateVesselRefuelRequest request);

  VesselRefuelResponse update(Integer id, UpdateVesselRefuelRequest request);

  void delete(Integer id);
}
