package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.vesselcolor.CreateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.UpdateVesselColorRequest;
import com.mtl.qcvmt.dto.vesselcolor.VesselColorResponse;
import com.mtl.qcvmt.service.VesselColorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vessel-colors")
@PreAuthorize("hasRole('qcvmt-admin')")
public class VesselColorController {

  private final VesselColorService vesselColorService;

  public VesselColorController(VesselColorService vesselColorService) {
    this.vesselColorService = vesselColorService;
  }

  @GetMapping
  public ApiResponse<List<VesselColorResponse>> list() {
    return ApiResponse.ok(vesselColorService.list());
  }

  @GetMapping("/{id}")
  public ApiResponse<VesselColorResponse> get(@PathVariable Integer id) {
    return ApiResponse.ok(vesselColorService.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<VesselColorResponse> create(@Valid @RequestBody CreateVesselColorRequest request) {
    VesselColorResponse created = vesselColorService.create(request);
    return ApiResponse.ok("Created", created);
  }

  @PutMapping("/{id}")
  public ApiResponse<VesselColorResponse> update(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateVesselColorRequest request) {
    VesselColorResponse updated = vesselColorService.update(id, request);
    return ApiResponse.ok("Updated", updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Integer id) {
    vesselColorService.delete(id);
    return ApiResponse.ok("Deleted", null);
  }
}
