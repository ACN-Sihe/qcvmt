package com.mtl.qcvmt.dto.colorset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateColorSetRequest(@NotBlank @Size(max=20)String boxcase,@NotBlank @Size(max=20)String color){}
