package com.mtl.qcvmt.dto.vessel;

import jakarta.validation.constraints.Size;

public record UpdateVesselRequest(@Size(max=10)String rowStart,@Size(max=10)String rowEnd,@Size(max=10)String tierStart,@Size(max=10)String tierEnd){}
