package com.mtl.qcvmt.util;

import com.mtl.qcvmt.dto.operationlog.OperationLogResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExportHandler {

  public void exportOperationLog(HttpServletResponse response, List<OperationLogResponse> logs) throws IOException {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=operation_logs.xlsx");

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("operation_logs");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("username");
      header.createCell(1).setCellValue("function_name");
      header.createCell(2).setCellValue("action_type");
      header.createCell(3).setCellValue("old_values");
      header.createCell(4).setCellValue("new_values");
      header.createCell(5).setCellValue("timestamp");

      int rowIdx = 1;
      for (OperationLogResponse log : logs) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(value(log.username()));
        row.createCell(1).setCellValue(value(log.functionName()));
        row.createCell(2).setCellValue(value(log.actionType()));
        row.createCell(3).setCellValue(value(log.oldValues()));
        row.createCell(4).setCellValue(value(log.newValues()));
        row.createCell(5).setCellValue(log.timestamp() == null ? "" : log.timestamp().toString());
      }

      workbook.write(response.getOutputStream());
    }
  }

  private String value(String value) {
    return value == null ? "" : value;
  }
}
