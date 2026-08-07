package com.mtl.qcvmt.dto.cellmatrix;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCellMatrixRequest(@NotBlank @Size(max=10)String type,@NotBlank @Size(max=10)String row,@NotBlank @Size(max=10)String tier,@Size(max=10)String tierStart,@Size(max=10)String tierEnd,@Size(max=1)String active){}
