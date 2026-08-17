package com.mtl.qcvmt.service.n4;

import com.mtl.qcvmt.dto.response.BayCellResponse;
import com.mtl.qcvmt.entity.CellMatrix;
import com.mtl.qcvmt.entity.Vessel;
import com.mtl.qcvmt.n4.N4QueryRepository;
import com.mtl.qcvmt.n4.N4TableConstants;
import com.mtl.qcvmt.repository.CellMatrixRepository;
import com.mtl.qcvmt.repository.VesselRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class N4VesselQueryService {

  private final N4QueryRepository n4QueryRepository;
  private final CellMatrixRepository cellMatrixRepository;
  private final VesselRepository vesselRepository;

  public N4VesselQueryService(
      N4QueryRepository n4QueryRepository,
      CellMatrixRepository cellMatrixRepository,
      VesselRepository vesselRepository) {
    this.n4QueryRepository = n4QueryRepository;
    this.cellMatrixRepository = cellMatrixRepository;
    this.vesselRepository = vesselRepository;
  }

  public List<BayCellResponse> getBayCells(String vesselId, String bay, String qdeck) {
    if (vesselId == null || vesselId.isBlank()
        || bay == null || bay.isBlank()
        || qdeck == null || qdeck.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "vesselId, bay and deckHold are required for bay layout");
    }

    Vessel v = findConfiguredVessel(vesselId, qdeck, bay)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Bay layout not found for vessel=" + vesselId + ", bay=" + bay + ", deckHold=" + qdeck));
    List<CellMatrix> configuredRows = cellMatrixRepository.findByTypeAndRowBetweenOrderByIdDesc(
        qdeck, formatPosition(requireNumber(v.getRowStart(), "rowStart")),
        formatPosition(requireNumber(v.getRowEnd(), "rowEnd")));
    if (configuredRows.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Bay matrix rows not found for deckHold=" + qdeck + ", rowStart=" + v.getRowStart()
              + ", rowEnd=" + v.getRowEnd());
    }
    return expandConfiguredRows(configuredRows, v.getTierStart(), v.getTierEnd(), qdeck);
  }

  private Optional<Vessel> findConfiguredVessel(String vesselId, String deckHold, String bay) {
    Optional<Vessel> exact =
        vesselRepository.findByVesselIdAndDeckHoldAndBay(vesselId, deckHold, bay);
    if (exact.isPresent()) {
      return exact;
    }

    Integer bayNumber = parseNumber(bay);
    if (bayNumber == null || bayNumber <= 0) {
      return Optional.empty();
    }
    String previousBay = String.format("%0" + Math.max(2, bay.length()) + "d", bayNumber - 1);
    return vesselRepository.findByVesselIdAndDeckHoldAndBay(vesselId, deckHold, previousBay);
  }

  private List<BayCellResponse> expandConfiguredRows(
      List<CellMatrix> configuredRows,
      String tierStartValue,
      String tierEndValue,
      String deckHold) {
    int tierStart = requireNumber(tierStartValue, "tierStart");
    int tierEnd = requireNumber(tierEndValue, "tierEnd");
    if (tierStart > tierEnd) {
      throw new IllegalStateException("Bay tier range start must not exceed end");
    }

    List<BayCellResponse> cells = new ArrayList<>();
    for (CellMatrix configuredRow : configuredRows) {
      for (int tier = tierEnd; tier >= tierStart; tier -= 2) {
        if ("B".equalsIgnoreCase(deckHold) && tier == 0) {
          continue;
        }
        cells.add(BayCellResponse.empty(
            formatPosition(requireNumber(configuredRow.getRow(), "row")),
            formatPosition(tier)));
      }
    }
    return cells;
  }

  private int requireNumber(String value, String field) {
    Integer parsed = parseNumber(value);
    if (parsed == null) {
      throw new IllegalStateException("Invalid bay " + field + ": " + value);
    }
    return parsed;
  }

  private Integer parseNumber(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private String formatPosition(int value) {
    return String.format("%02d", value);
  }

  public String getVesselName(String vesselId) {
    String sql = "SELECT vv.name FROM " + N4TableConstants.ARGO_CARRIER_VISIT + " acv "
        + "JOIN " + N4TableConstants.VSL_VISIT_DETAILS + " vvd ON vvd.vvd_gkey = acv.cvcvd_gkey "
        + "JOIN " + N4TableConstants.VSL_VESSELS + " vv ON vv.gkey = vvd.vessel_gkey "
        + "WHERE acv.id = ? FETCH FIRST 1 ROWS ONLY";
    return n4QueryRepository.queryForObject(sql, String.class, vesselId);
  }
}
