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
import org.springframework.stereotype.Service;

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
    String effectiveDeck = qdeck == null || qdeck.isBlank() ? "A" : qdeck;
    if (qdeck == null || qdeck.isBlank()) {
      return expandDefaultMatrix(
          cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(effectiveDeck, "1"), effectiveDeck);
    }

    if (vesselId == null || vesselId.isBlank() || bay == null || bay.isBlank()) {
      return expandDefaultMatrix(
          cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(effectiveDeck, "1"), effectiveDeck);
    }

    // NOTE: t_vessel is our own local bay/tier layout table (see Vessel entity),
    // not an N4 Oracle table. It was previously (incorrectly) queried through
    // n4QueryRepository against the N4 datasource, which always failed with an
    // invalid-table error and silently fell back to the generic Bay Size default
    // matrix (whose "tier" column holds a configured tier *count*, e.g. "11",
    // not the vessel's real physical tier numbers). Read it from the local
    // VesselRepository instead so the real tierStart/tierEnd for this
    // vessel+bay+deck is used.
    Optional<Vessel> vessel = vesselRepository.findByVesselIdAndDeckHoldAndBay(vesselId, qdeck, bay);
    if (vessel.isEmpty()) {
      return expandDefaultMatrix(
          cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(effectiveDeck, "1"), effectiveDeck);
    }

    Vessel v = vessel.get();
    return expandRange(v.getRowStart(), v.getRowEnd(), v.getTierStart(), v.getTierEnd(), "1");
  }

  private List<BayCellResponse> expandDefaultMatrix(List<CellMatrix> matrix, String deckHold) {
    List<BayCellResponse> cells = new ArrayList<>();
    for (CellMatrix rowConfig : matrix) {
      Integer tierStart = parseNumber(rowConfig.getTierStart());
      Integer tierEnd = parseNumber(rowConfig.getTierEnd());
      if (tierStart != null && tierEnd != null) {
        cells.addAll(expandRange(
            rowConfig.getRow(), rowConfig.getRow(), rowConfig.getTierStart(),
            rowConfig.getTierEnd(), rowConfig.getActive()));
        continue;
      }

      int tierIndexEnd = requireNumber(rowConfig.getTier(), "tier");
      for (int index = 0; index <= tierIndexEnd; index += 2) {
        int tier = "B".equalsIgnoreCase(deckHold) ? index * 2 : 78 + index * 2;
        cells.add(new BayCellResponse(
            formatPosition(requireNumber(rowConfig.getRow(), "row")),
            formatPosition(tier),
            rowConfig.getActive()));
      }
    }
    return cells;
  }

  private List<BayCellResponse> expandRange(
      String rowStartValue,
      String rowEndValue,
      String tierStartValue,
      String tierEndValue,
      String active) {
    int rowStart = requireNumber(rowStartValue, "rowStart");
    int rowEnd = requireNumber(rowEndValue, "rowEnd");
    int tierStart = requireNumber(tierStartValue, "tierStart");
    int tierEnd = requireNumber(tierEndValue, "tierEnd");
    if (rowStart > rowEnd || tierStart > tierEnd) {
      throw new IllegalStateException("Bay row/tier range start must not exceed end");
    }

    List<BayCellResponse> cells = new ArrayList<>();
    for (int row = rowStart; row <= rowEnd; row += 2) {
      for (int tier = tierStart; tier <= tierEnd; tier += 2) {
        cells.add(new BayCellResponse(formatPosition(row), formatPosition(tier), active));
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
