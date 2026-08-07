package com.mtl.qcvmt.service;

import com.mtl.qcvmt.dto.vesselcolor.CreateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.UpdateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.VesselColorResponse;
import java.util.List;

public interface VesselColorService {

  List<VesselColorResponse> list();

  VesselColorResponse get(Integer id);

  VesselColorResponse create(CreateVesselColorRequest request);

  VesselColorResponse update(Integer id, UpdateVesselColorRequest request);

  void delete(Integer id);
}
