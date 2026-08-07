package com.mtl.qcvmt.service.n4;

import com.mtl.qcvmt.dto.response.WorkQueueResult;
import com.mtl.qcvmt.entity.SequenceVO;
import com.mtl.qcvmt.n4.N4QueryRepository;
import com.mtl.qcvmt.n4.N4TableConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class N4WorkQueueService {

  private final N4QueryRepository n4QueryRepository;

  public N4WorkQueueService(N4QueryRepository n4QueryRepository) {
    this.n4QueryRepository = n4QueryRepository;
  }

  public String getLoadOrder(String qcid) {
    String sql = "SELECT MIN(iq.qorder) FROM " + N4TableConstants.INV_WQ + " iq "
        + "JOIN " + N4TableConstants.XPS_CRANESHIFT + " xcs ON iq.first_shift_pkey = xcs.pkey "
        + "JOIN " + N4TableConstants.XPS_POINTOFWORK + " xpow ON xcs.owner_pow = xpow.pkey "
        + "WHERE iq.qtype IN ('LOAD') AND xpow.name = ?";
    return n4QueryRepository.queryForObject(sql, String.class, qcid);
  }

  public String getDischargeOrder(String qcid) {
    String sql = "SELECT MIN(iq.qorder) FROM " + N4TableConstants.INV_WQ + " iq "
        + "JOIN " + N4TableConstants.XPS_CRANESHIFT + " xcs ON iq.first_shift_pkey = xcs.pkey "
        + "JOIN " + N4TableConstants.XPS_POINTOFWORK + " xpow ON xcs.owner_pow = xpow.pkey "
        + "WHERE iq.qtype IN ('DISCH') AND xpow.name = ?";
    return n4QueryRepository.queryForObject(sql, String.class, qcid);
  }

  public WorkQueueResult getCurrentWorkQueue(String qcNum) {
    WorkQueueResult load = buildWorkQueueResult(qcNum, "LOAD", safeGetLoadOrder(qcNum));
    if (load != null) {
      return load;
    }

    WorkQueueResult disch = buildWorkQueueResult(qcNum, "DISCH", safeGetDischargeOrder(qcNum));
    if (disch != null) {
      return disch;
    }

    return new WorkQueueResult("UNKNOWN", null, null, null, null, null, Collections.emptyList());
  }

  public List<SequenceVO> getSequenceList(String qorder, String qtype) {
    if (qorder == null || qorder.isBlank() || qtype == null || qtype.isBlank()) {
      return Collections.emptyList();
    }
    String sql = "SELECT iq.curr_pos_slot, iq.planned_pos_slot, iq.qtype, iq.qdeck, iq.qrow, iq.status, iq.qbay "
        + "FROM " + N4TableConstants.INV_WQ + " iq WHERE iq.qorder = ? AND iq.qtype = ? ORDER BY iq.qrow";
    List<Map<String, Object>> rows = n4QueryRepository.queryForList(sql, qorder, qtype);
    List<SequenceVO> sequences = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      SequenceVO sequence = new SequenceVO();
      sequence.setCurrentPosSlot((String) row.get("curr_pos_slot"));
      sequence.setPlannedPosSlot((String) row.get("planned_pos_slot"));
      sequence.setQtype((String) row.get("qtype"));
      sequence.setQdeck((String) row.get("qdeck"));
      sequence.setQrow((String) row.get("qrow"));
      sequence.setStatus((String) row.get("status"));
      sequence.setBay((String) row.get("qbay"));
      sequences.add(sequence);
    }
    return sequences;
  }

  public boolean checkSequenceList(String qorder) {
    if (qorder == null || qorder.isBlank()) {
      return false;
    }
    String sql = "SELECT COUNT(1) FROM " + N4TableConstants.INV_WQ + " WHERE qorder = ?";
    Integer count = n4QueryRepository.queryForObject(sql, Integer.class, qorder);
    return count != null && count > 0;
  }

  private WorkQueueResult buildWorkQueueResult(String qcid, String qtype, String qorder) {
    if (qorder == null || qorder.isBlank() || !checkSequenceList(qorder)) {
      return null;
    }

    List<SequenceVO> sequences = getSequenceList(qorder, qtype);
    if (sequences.isEmpty()) {
      return null;
    }

    String sql = "SELECT MIN(iq.qbay) AS min_bay, MAX(iq.qbay) AS max_bay, MIN(iq.pos_locid) AS vessel_id, "
        + "MIN(iq.qdeck) AS deck_hold FROM " + N4TableConstants.INV_WQ + " iq "
        + "JOIN " + N4TableConstants.XPS_CRANESHIFT + " xcs ON iq.first_shift_pkey = xcs.pkey "
        + "JOIN " + N4TableConstants.XPS_POINTOFWORK + " xpow ON xcs.owner_pow = xpow.pkey "
        + "WHERE iq.qorder = ? AND iq.qtype = ? AND xpow.name = ?";
    Map<String, Object> meta = n4QueryRepository.queryForMap(sql, qorder, qtype, qcid);

    return new WorkQueueResult(
        qtype,
        qorder,
        value(meta.get("vessel_id")),
        value(meta.get("min_bay")),
        value(meta.get("max_bay")),
        value(meta.get("deck_hold")),
        sequences);
  }

  private String safeGetLoadOrder(String qcid) {
    try {
      return getLoadOrder(qcid);
    } catch (RuntimeException ex) {
      return null;
    }
  }

  private String safeGetDischargeOrder(String qcid) {
    try {
      return getDischargeOrder(qcid);
    } catch (RuntimeException ex) {
      return null;
    }
  }

  private String value(Object obj) {
    return obj == null ? null : obj.toString();
  }
}
