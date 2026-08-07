package com.mtl.qcvmt.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(@Size(max=20)String qcid,@Size(max=10)String role,@Size(max=20)String parent){}
