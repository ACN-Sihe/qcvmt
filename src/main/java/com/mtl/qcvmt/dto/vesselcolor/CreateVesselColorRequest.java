package com.mtl.qcvmt.dto.vesselcolor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVesselColorRequest(@NotBlank @Size(max=10)String vesselId,@NotBlank @Size(max=10)String deckHold,@NotBlank @Size(max=10)String bay,@Size(max=10)String rowStart,@Size(max=10)String rowEnd,@Size(max=10)String tierStart,@Size(max=10)String tierEnd){}
