package com.mtl.qcvmt.service.n4;

import com.mtl.qcvmt.n4.N4QueryRepository;
import com.mtl.qcvmt.n4.N4TableConstants;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class N4FacilityQueryService {

  private final N4QueryRepository n4QueryRepository;
  private final String company;

  public N4FacilityQueryService(N4QueryRepository n4QueryRepository, @Value("${qcvmt.n4.company:MTL}") String company) {
    this.n4QueryRepository = n4QueryRepository;
    this.company = company;
  }

  public List<String> queryQcId() {
    String sql = "SELECT DISTINCT xpow.name AS qcid FROM " + N4TableConstants.XPS_POINTOFWORK + " xpow "
        + "WHERE xpow.yard IN (SELECT gkey FROM " + N4TableConstants.ARGO_YARD + " "
        + "WHERE fcy_gkey IN (SELECT gkey FROM " + N4TableConstants.ARGO_FACILITY + " WHERE name = ?))";
    List<Map<String, Object>> rows = n4QueryRepository.queryForList(sql, company);
    return rows.stream().map(row -> String.valueOf(row.get("qcid"))).toList();
  }

  public String queryFacilityByQcId(String qcid) {
    String sql = "SELECT af.name FROM " + N4TableConstants.ARGO_FACILITY + " af "
        + "JOIN " + N4TableConstants.ARGO_YARD + " ay ON af.gkey = ay.fcy_gkey "
        + "JOIN " + N4TableConstants.XPS_POINTOFWORK + " xpow ON ay.gkey = xpow.yard "
        + "WHERE xpow.name = ? FETCH FIRST 1 ROWS ONLY";
    return n4QueryRepository.queryForObject(sql, String.class, qcid);
  }
}
