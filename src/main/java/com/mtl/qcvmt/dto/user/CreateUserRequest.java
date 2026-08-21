package com.mtl.qcvmt.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@NotBlank @Size(max=20)String username,@Size(max=20)String qcid,@NotBlank @Size(max=10)String role,@Size(max=20)String parent){}
