package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.colorset.ColorSetResponse;
import com.mtl.qcvmt.dto.colorset.CreateColorSetRequest;
import com.mtl.qcvmt.dto.colorset.UpdateColorSetRequest;
import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.service.ColorSetService;
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
@RequestMapping("/api/color-sets")
@PreAuthorize("hasRole('qcvmt-admin')")
public class ColorSetController {

  private final ColorSetService colorSetService;

  public ColorSetController(ColorSetService colorSetService) {
    this.colorSetService = colorSetService;
  }

  @GetMapping
  public ApiResponse<List<ColorSetResponse>> list() {
    return ApiResponse.ok(colorSetService.list());
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
