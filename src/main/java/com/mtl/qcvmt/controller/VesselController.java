package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.vessel.CreateVesselRequest;
import com.mtl.qcvmt.dto.vessel.UpdateVesselRequest;
import com.mtl.qcvmt.dto.vessel.VesselResponse;
import com.mtl.qcvmt.service.VesselService;
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
@RequestMapping("/api/vessels")
@PreAuthorize("hasRole('qcvmt-admin')")
public class VesselController {

  private final VesselService vesselService;

  public VesselController(VesselService vesselService) {
    this.vesselService = vesselService;
  }

  @GetMapping
  public ApiResponse<List<VesselResponse>> list() {
    return ApiResponse.ok(vesselService.list());
  }

  @GetMapping("/{id}")
  public ApiResponse<VesselResponse> get(@PathVariable Integer id) {
    return ApiResponse.ok(vesselService.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<VesselResponse> create(@Valid @RequestBody CreateVesselRequest request) {
    VesselResponse created = vesselService.create(request);
    return ApiResponse.ok("Created", created);
  }

  @PutMapping("/{id}")
  public ApiResponse<VesselResponse> update(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateVesselRequest request) {
    VesselResponse updated = vesselService.update(id, request);
    return ApiResponse.ok("Updated", updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Integer id) {
    vesselService.delete(id);
    return ApiResponse.ok("Deleted", null);
  }
}
