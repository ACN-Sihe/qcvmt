package com.mtl.qcvmt.service.n4;

import com.mtl.qcvmt.dto.response.RobContainer;
import com.mtl.qcvmt.n4.N4QueryRepository;
import com.mtl.qcvmt.n4.N4TableConstants;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class N4ContainerQueryService {

  private final N4QueryRepository n4QueryRepository;

  public N4ContainerQueryService(N4QueryRepository n4QueryRepository) {
    this.n4QueryRepository = n4QueryRepository;
  }

  public List<RobContainer> getROBList(String vesselId, String minBay) {
    if (vesselId == null || vesselId.isBlank()) {
      return Collections.emptyList();
    }
    String sql = "SELECT iufv.flex_string01 AS bay, iufv.last_pos_slot AS slot, iu.id AS container_id "
        + "FROM " + N4TableConstants.INV_UNIT_FCY_VISIT + " iufv "
        + "JOIN " + N4TableConstants.INV_UNIT + " iu ON iufv.unit_gkey = iu.gkey "
        + "JOIN " + N4TableConstants.ARGO_CARRIER_VISIT + " acv ON iufv.facility_gkey = acv.facility_gkey "
        + "WHERE acv.id = ? AND SUBSTR(iufv.flex_string01, 1, 2) >= ? AND iufv.departure_visit_gkey IS NULL";
    List<Map<String, Object>> rows = n4QueryRepository.queryForList(sql, vesselId, minBay == null ? "00" : minBay);
    return rows.stream().map(this::toRobContainer).toList();
  }

  public List<RobContainer> getROBListByBay(String vesselId, String bay) {
    if (vesselId == null || vesselId.isBlank() || bay == null || bay.isBlank()) {
      return Collections.emptyList();
    }
    String sql = "SELECT iufv.flex_string01 AS bay, iufv.last_pos_slot AS slot, iu.id AS container_id "
        + "FROM " + N4TableConstants.INV_UNIT_FCY_VISIT + " iufv "
        + "JOIN " + N4TableConstants.INV_UNIT + " iu ON iufv.unit_gkey = iu.gkey "
        + "JOIN " + N4TableConstants.ARGO_CARRIER_VISIT + " acv ON iufv.facility_gkey = acv.facility_gkey "
        + "WHERE acv.id = ? AND iufv.flex_string01 = ? AND iufv.departure_visit_gkey IS NULL";
    List<Map<String, Object>> rows = n4QueryRepository.queryForList(sql, vesselId, bay);
    return rows.stream().map(this::toRobContainer).toList();
  }

  public List<Map<String, Object>> getHazardList(String unitId) {
    String sql = "SELECT hm.unno, hm.proper_name FROM " + N4TableConstants.REF_HAZARDOUS_MATERIAL + " hm "
        + "JOIN " + N4TableConstants.INV_UNIT + " iu ON iu.goods = hm.gkey WHERE iu.id = ?";
    return n4QueryRepository.queryForList(sql, unitId);
  }

  public List<Map<String, Object>> getTwentyUnitList(String qcid, String vesselId, String bay) {
    String sql = "SELECT iu.id, iu.category FROM " + N4TableConstants.INV_UNIT + " iu "
        + "WHERE iu.line_op = ? AND iu.cv_id = ? AND iu.last_pos_slot LIKE ?";
    return n4QueryRepository.queryForList(sql, qcid, vesselId, bay + "%");
  }

  private RobContainer toRobContainer(Map<String, Object> row) {
    String bay = value(row.get("bay"));
    String slot = value(row.get("slot"));
    String containerId = value(row.get("container_id"));
    String rowNo = slot != null && slot.length() >= 5 ? slot.substring(2, 4) : null;
    String tier = slot != null && slot.length() >= 7 ? slot.substring(4, 6) : null;
    return new RobContainer(bay, rowNo, tier, containerId, null);
  }

  private String value(Object value) {
    return value == null ? null : value.toString();
  }
}
