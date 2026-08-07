package com.mtl.qcvmt.util;

import com.mtl.qcvmt.dto.vessel.CreateVesselRequest;
import com.mtl.qcvmt.exception.BusinessException;
import com.mtl.qcvmt.service.VesselService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImportHandler {

  private final VesselService vesselService;

  public ImportHandler(VesselService vesselService) {
    this.vesselService = vesselService;
  }

  public Path uploadFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException("empty_file");
    }
    try {
      Path tempFile = Files.createTempFile("qcvmt-vessel-import-", ".xlsx");
      file.transferTo(tempFile);
      return tempFile;
    } catch (IOException ex) {
      throw new BusinessException("upload_failed");
    }
  }

  public int importVessel(MultipartFile file) {
    Path path = uploadFile(file);
    int imported = 0;
    try (InputStream inputStream = Files.newInputStream(path); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0);
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }
        CreateVesselRequest request = new CreateVesselRequest(
            text(row.getCell(0)),
            text(row.getCell(1)),
            text(row.getCell(2)),
            text(row.getCell(3)),
            text(row.getCell(4)),
            text(row.getCell(5)),
            text(row.getCell(6)));
        vesselService.create(request);
        imported++;
      }
      return imported;
    } catch (IOException ex) {
      throw new BusinessException("import_failed");
    } finally {
      try {
        Files.deleteIfExists(path);
      } catch (IOException ignore) {
      }
    }
  }

  private String text(Cell cell) {
    return cell == null ? null : cell.toString().trim();
  }
}
