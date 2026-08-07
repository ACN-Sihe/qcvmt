package com.mtl.qcvmt.dto.vesselrefuel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVesselRefuelRequest(@NotBlank @Size(max=10)String vesselId,@NotBlank @Size(max=10)String isRefuel){}
