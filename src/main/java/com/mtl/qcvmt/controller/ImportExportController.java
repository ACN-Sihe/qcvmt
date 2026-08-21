package com.mtl.qcvmt.controller;

import com.mtl.qcvmt.dto.common.ApiResponse;
import com.mtl.qcvmt.service.OperationLogService;
import com.mtl.qcvmt.util.ExportHandler;
import com.mtl.qcvmt.util.ImportHandler;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@PreAuthorize("hasRole('qcvmt-admin')")
public class ImportExportController {

  private final ImportHandler importHandler;
  private final ExportHandler exportHandler;
  private final OperationLogService operationLogService;

  public ImportExportController(
      ImportHandler importHandler,
      ExportHandler exportHandler,
      OperationLogService operationLogService) {
    this.importHandler = importHandler;
    this.exportHandler = exportHandler;
    this.operationLogService = operationLogService;
  }

  @GetMapping("/api/export/logs")
  public void exportLogs(
      HttpServletResponse response,
      @RequestParam(value = "from", required = false) LocalDate from,
      @RequestParam(value = "to", required = false) LocalDate to) throws IOException {
    LocalDateTime fromTime = from == null ? LocalDate.now().minusMonths(1).atStartOfDay() : from.atStartOfDay();
    LocalDateTime toTime = to == null ? LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1)
        : to.plusDays(1).atStartOfDay().minusNanos(1);
    exportHandler.exportOperationLog(response, operationLogService.listByPeriod(fromTime, toTime));
  }

  @PostMapping("/api/import/vessel")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Integer> importVessel(@RequestParam("file") MultipartFile file) {
    int imported = importHandler.importVessel(file);
    return ApiResponse.ok("Imported", imported);
  }
}
