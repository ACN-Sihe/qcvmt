package com.mtl.qcvmt.dto.cellmatrix;

import jakarta.validation.constraints.Size;

public record UpdateCellMatrixRequest(@Size(max=10)String type,@Size(max=10)String row,@Size(max=10)String tier,@Size(max=10)String tierStart,@Size(max=10)String tierEnd,@Size(max=1)String active){}
