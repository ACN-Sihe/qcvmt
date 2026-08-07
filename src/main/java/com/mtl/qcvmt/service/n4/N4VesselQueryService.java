package com.mtl.qcvmt.service.n4;

import com.mtl.qcvmt.entity.CellMatrix;
import com.mtl.qcvmt.n4.N4QueryRepository;
import com.mtl.qcvmt.n4.N4TableConstants;
import com.mtl.qcvmt.repository.CellMatrixRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class N4VesselQueryService {

  private final N4QueryRepository n4QueryRepository;
  private final CellMatrixRepository cellMatrixRepository;

  public N4VesselQueryService(N4QueryRepository n4QueryRepository, CellMatrixRepository cellMatrixRepository) {
    this.n4QueryRepository = n4QueryRepository;
    this.cellMatrixRepository = cellMatrixRepository;
  }

  public List<CellMatrix> getCellMatrix(String vesselId, String bay, String qdeck) {
    if (qdeck == null || qdeck.isBlank()) {
      return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc("A", "1");
    }

    if (vesselId == null || vesselId.isBlank() || bay == null || bay.isBlank()) {
      return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(qdeck, "1");
    }

    String sql = "SELECT tv.row_start, tv.row_end, tv.tier_start, tv.tier_end FROM t_vessel tv "
        + "JOIN " + N4TableConstants.ARGO_CARRIER_VISIT + " acv ON acv.id = tv.vesselid "
        + "WHERE tv.vesselid = ? AND tv.bay = ? AND tv.deck_hold = ?";
    List<Map<String, Object>> rows;
    try {
      rows = n4QueryRepository.queryForList(sql, vesselId, bay, qdeck);
    } catch (RuntimeException ex) {
      return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(qdeck, "1");
    }

    if (rows.isEmpty()) {
      return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(qdeck, "1");
    }

    List<CellMatrix> matrix = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      CellMatrix cell = new CellMatrix();
      cell.setType(qdeck);
      cell.setRow(value(row.get("row_start")));
      cell.setTier(value(row.get("tier_start")));
      cell.setTierStart(value(row.get("tier_start")));
      cell.setTierEnd(value(row.get("tier_end")));
      cell.setActive("1");
      matrix.add(cell);
    }
    return matrix;
  }

  public String getVesselName(String vesselId) {
    String sql = "SELECT vv.name FROM " + N4TableConstants.VSL_VESSELS + " vv WHERE vv.id = ?";
    return n4QueryRepository.queryForObject(sql, String.class, vesselId);
  }

  private String value(Object value) {
    return value == null ? null : value.toString();
  }
}
