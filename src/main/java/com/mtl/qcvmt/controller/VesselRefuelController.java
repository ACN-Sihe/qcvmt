package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import com.mtl.qcvmt.dto.vesselrefuel.CreateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.UpdateVesselRefuelRequest;
import com.mtl.qcvmt.dto.vesselrefuel.VesselRefuelResponse;
import com.mtl.qcvmt.service.VesselRefuelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/vessel-refuels")
@PreAuthorize("hasRole('qcvmt-admin')")
@Validated
public class VesselRefuelController {

  private final VesselRefuelService vesselRefuelService;

  public VesselRefuelController(VesselRefuelService vesselRefuelService) {
    this.vesselRefuelService = vesselRefuelService;
  }

  @GetMapping
  public ApiResponse<PageResponse<VesselRefuelResponse>> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(vesselRefuelService.list(PageRequest.of(page, size, Sort.by("vesselId")), keyword));
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
