package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.colorset.ColorSetResponse;
import com.mtl.qcvmt.dto.colorset.CreateColorSetRequest;
import com.mtl.qcvmt.dto.colorset.UpdateColorSetRequest;
import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.dto.common.PageResponse;
import com.mtl.qcvmt.service.ColorSetService;
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
@RequestMapping("/api/color-sets")
@PreAuthorize("hasRole('qcvmt-admin')")
@Validated
public class ColorSetController {

  private final ColorSetService colorSetService;

  public ColorSetController(ColorSetService colorSetService) {
    this.colorSetService = colorSetService;
  }

  @GetMapping
  public ApiResponse<PageResponse<ColorSetResponse>> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
      @RequestParam(required = false) String keyword) {
    return ApiResponse.ok(colorSetService.list(PageRequest.of(page, size, Sort.by("boxcase")), keyword));
  }

  @GetMapping("/{id}")
  public ApiResponse<ColorSetResponse> get(@PathVariable Integer id) {
    return ApiResponse.ok(colorSetService.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ColorSetResponse> create(@Valid @RequestBody CreateColorSetRequest request) {
    ColorSetResponse created = colorSetService.create(request);
    return ApiResponse.ok("Created", created);
  }

  @PutMapping("/{id}")
  public ApiResponse<ColorSetResponse> update(
      @PathVariable Integer id,
      @Valid @RequestBody UpdateColorSetRequest request) {
    ColorSetResponse updated = colorSetService.update(id, request);
    return ApiResponse.ok("Updated", updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Integer id) {
    colorSetService.delete(id);
    return ApiResponse.ok("Deleted", null);
  }
}
