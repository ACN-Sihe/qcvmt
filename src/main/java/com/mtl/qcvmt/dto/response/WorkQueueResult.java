package com.mtl.qcvmt.dto.response;

import com.mtl.qcvmt.entity.SequenceVO;
import java.util.List;

public record WorkQueueResult(String qType,String qorder,String vesselId,String minBay,String maxBay,String deckHold,List<SequenceVO>sequences){}
