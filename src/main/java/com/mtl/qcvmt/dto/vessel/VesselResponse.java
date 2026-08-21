package com.mtl.qcvmt.dto.vessel;

public record VesselResponse(Integer id,String vesselId,String deckHold,String bay,String rowStart,String rowEnd,String tierStart,String tierEnd,Integer version){}
