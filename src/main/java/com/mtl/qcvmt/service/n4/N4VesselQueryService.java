package com.mtl.qcvmt.service.n4;

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

  public List<CellMatrix> getCellMatrix(String vesselId, String bay, String qdeck) {
    if (qdeck == null || qdeck.isBlank()) {
      return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc("A", "1");
    }

    if (vesselId == null || vesselId.isBlank() || bay == null || bay.isBlank()) {
      return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(qdeck, "1");
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
      return cellMatrixRepository.findByTypeAndActiveOrderByIdDesc(qdeck, "1");
    }

    Vessel v = vessel.get();
    List<CellMatrix> matrix = new ArrayList<>();
    CellMatrix cell = new CellMatrix();
    cell.setType(qdeck);
    cell.setRow(v.getRowStart());
    cell.setTier(v.getTierStart());
    cell.setTierStart(v.getTierStart());
    cell.setTierEnd(v.getTierEnd());
    cell.setActive("1");
    matrix.add(cell);
    return matrix;
  }

  public String getVesselName(String vesselId) {
    String sql = "SELECT vv.name FROM " + N4TableConstants.ARGO_CARRIER_VISIT + " acv "
        + "JOIN " + N4TableConstants.VSL_VISIT_DETAILS + " vvd ON vvd.vvd_gkey = acv.cvcvd_gkey "
        + "JOIN " + N4TableConstants.VSL_VESSELS + " vv ON vv.gkey = vvd.vessel_gkey "
        + "WHERE acv.id = ? FETCH FIRST 1 ROWS ONLY";
    return n4QueryRepository.queryForObject(sql, String.class, vesselId);
  }
}
