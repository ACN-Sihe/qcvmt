package com.mtl.qcvmt.dto.vesselcolor;

import jakarta.validation.constraints.Size;

public record UpdateVesselColorRequest(@Size(max=10)String rowStart,@Size(max=10)String rowEnd,@Size(max=10)String tierStart,@Size(max=10)String tierEnd){}
