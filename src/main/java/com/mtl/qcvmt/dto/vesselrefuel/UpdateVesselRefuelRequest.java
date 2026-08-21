package com.mtl.qcvmt.dto.vesselrefuel;

import jakarta.validation.constraints.Size;

public record UpdateVesselRefuelRequest(@Size(max=10)String isRefuel){}
