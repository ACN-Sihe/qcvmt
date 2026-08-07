package com.mtl.qcvmt.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBaySizeRequest(@NotNull Integer id,@Size(max=10)String type,@Size(max=10)String bay,@Size(max=10)String deckHold,@Size(max=10)String rowStart,@Size(max=10)String rowEnd,@Size(max=10)String tierStart,@Size(max=10)String tierEnd,@Size(max=1)String active){}
