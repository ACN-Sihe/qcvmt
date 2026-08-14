package com.mtl.qcvmt.service.n4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mtl.qcvmt.dto.response.BayCellResponse;
import com.mtl.qcvmt.entity.Vessel;
import com.mtl.qcvmt.n4.N4QueryRepository;
import com.mtl.qcvmt.repository.CellMatrixRepository;
import com.mtl.qcvmt.repository.VesselRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class N4VesselQueryServiceTest {

  @Mock
  private N4QueryRepository n4QueryRepository;
  @Mock
  private CellMatrixRepository cellMatrixRepository;
  @Mock
  private VesselRepository vesselRepository;

  @Test
  void expandsConfiguredBayIntoEveryPhysicalRowAndTierCell() {
    Vessel vessel = new Vessel(1, "VESSEL-1", "A", "17", "01", "05", "82", "86", 0);
    when(vesselRepository.findByVesselIdAndDeckHoldAndBay("VESSEL-1", "A", "17"))
        .thenReturn(Optional.of(vessel));

    N4VesselQueryService service =
        new N4VesselQueryService(n4QueryRepository, cellMatrixRepository, vesselRepository);

    List<BayCellResponse> cells = service.getBayCells("VESSEL-1", "17", "A");

    assertThat(cells).containsExactly(
        new BayCellResponse("01", "82", "1"),
        new BayCellResponse("01", "84", "1"),
        new BayCellResponse("01", "86", "1"),
        new BayCellResponse("03", "82", "1"),
        new BayCellResponse("03", "84", "1"),
        new BayCellResponse("03", "86", "1"),
        new BayCellResponse("05", "82", "1"),
        new BayCellResponse("05", "84", "1"),
        new BayCellResponse("05", "86", "1"));
  }
}
