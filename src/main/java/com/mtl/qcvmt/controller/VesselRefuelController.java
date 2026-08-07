package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.vesselrefuel.CreateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.UpdateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.VesselRefuelResponse;
import com.mtl.qcvmt.service.VesselRefuelService;
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
@RequestMapping("/api/vessel-refuels")
@PreAuthorize("hasRole('qcvmt-admin')")
public class VesselRefuelController {

  private final VesselRefuelService vesselRefuelService;

  public VesselRefuelController(VesselRefuelService vesselRefuelService) {
    this.vesselRefuelService = vesselRefuelService;
  }

  @GetMapping
  public ApiResponse<List<VesselRefuelResponse>> list() {
    return ApiResponse.ok(vesselRefuelService.list());
  }

  @GetMapping("/{id}")
  public ApiResponse<VesselRefuelResponse> get(@PathVariable Integer id) {
    return ApiResponse.ok(vesselRefuelService.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<VesselRefuelResponse> create(@Valid @RequestBody CreateVesselRefuelRequest request) {
    VesselRefuelResponse created = vesselRefuelService.create(request);
    return ApiResponse.ok("Created", created);
  }

  @PutMapping("/{id}")
  public ApiResponse<VesselRefuelResponse> update(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateVesselRefuelRequest request) {
    VesselRefuelResponse updated = vesselRefuelService.update(id, request);
    return ApiResponse.ok("Updated", updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Integer id) {
    vesselRefuelService.delete(id);
    return ApiResponse.ok("Deleted", null);
  }
}
