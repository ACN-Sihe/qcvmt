package com.mtl.qcvmt.dto.colorset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateColorSetRequest(@NotBlank @Size(max=20)String color){}
