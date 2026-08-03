package com.springMVC.entity;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class SequenceVO {
		
		private String current_pos_slot="";
		private String qtype="";
		private String planned_pos_slot="";
		private String pos_locid="";
		private String qc_id="";
		private String qdeck="";
		private String qrow="";
		private String bay;
		private float sequence;
		private String is_oog="";
		private String is_powered="";
		private String istank="";
		private String isquad="";
		private String istandem="";
		private String istwin="";
		private String issingle="";
		private String status="";
		private String moveStage="";
		private String complexunit=""; //"" means only one bay has unit, "0" means two bay has unit,front and rear is the same. "1" means two bay has unit,front and rear is different.
		/*Begin PCR-VMT-000004 Tony Add */
		private String twentyInd;
		/*End PCR-VMT-000004 Tony Add*/
		/*Start PCR-DG*/
		private String is_dg;
		/*End PCR-DG*/
		public String getComplexunit() {
			return complexunit;
		}
		public void setComplexunit(String complexunit) {
			this.complexunit = complexunit;
		}
		
		public String getIsSingle() {
			return issingle;
		}
		public void setIsSingle(String issingle) {
			this.issingle = issingle;
		}
		
		public String getIsQuad() {
			return isquad;
		}
		public void setIsQuad(String isquad) {
			this.isquad = isquad;
		}
		
		public String getIsTandem() {
			return istandem;
		}
		public void setIsTandem(String istandem) {
			this.istandem = istandem;
		}
		
		public String getIsTwin() {
			return istwin;
		}
		public void setIsTwin(String istwin) {
			this.istwin = istwin;
		}
		
		public String getBay() {
			return bay;
		}
		public void setBay(String bay) {
			this.bay = bay;
		}
		
		
		public String getMoveStage() {
			return moveStage;
		}
		public void setMoveStage(String moveStage) {
			this.moveStage = moveStage;
		}
		
		
		public String getIstank() {
			return istank;
		}
		public void setIstank(String istank) {
			this.istank = istank;
		}
		
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		
		public String getCurrent_pos_slot() {
			return current_pos_slot;
		}
		public void setCurrent_pos_slot(String current_pos_slot) {
			this.current_pos_slot = current_pos_slot;
		}
		public String getQtype() {
			return qtype;
		}
		public void setQtype(String qtype) {
			this.qtype = qtype;
		}
		public String getPlanned_pos_slot() {
			return planned_pos_slot;
		}
		public void setPlanned_pos_slot(String planned_pos_slot) {
			this.planned_pos_slot = planned_pos_slot;
		}
		
	
		public String getPos_locid() {
			return pos_locid;
		}
		public void setPos_locid(String pos_locid) {
			this.pos_locid = pos_locid;
		}
		public String getQc_id() {
			return qc_id;
		}
		public void SetQc_id(String qc_id) {
			this.qc_id = qc_id;
		}
		public String getQdeck() {
			return qdeck;
		}
		public void setQdeck(String qdeck) {
			this.qdeck = qdeck;
		}
		
		public String getQrow() {
			return qrow;
		}
		public void setQrow(String qrow) {
			this.qrow = qrow;
		}
		
		public float getSequence() {
			return sequence;
		}
		public void setSequence(float sequence) {
			this.sequence = sequence;
		}
		public String getIs_oog() {
			return is_oog;
		}
		public void setIs_oog(String is_oog) {
			this.is_oog = is_oog;
		}
		
		
		public String getIs_powered() {
			return is_powered;
		}
		public void setIs_powered(String is_powered) {
			this.is_powered = is_powered;
		}
		/*Begin PCR-VMT-000004 Tony Add*/
		public String getTwentyInd() {
			return twentyInd;
		}
		public void setTwentyInd(String twentyInd) {
			this.twentyInd = twentyInd;
		}
		/*End PCR-VMT-000004 Tony Add*/
	
		/*Start PCR-DG*/
		public String getIs_dg(){
			return is_dg;
		}
		public void setIs_dg(String is_dg){
			this.is_dg = is_dg;
		}
		/*End PCR-DG*/

}
