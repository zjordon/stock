package com.jason.stock.bean;

public class ChipInLog {

	private String id;
	private String stockId;
	private int logDate;
	private String weight;
	
	public ChipInLog(String stockId, int logDate, String weight) {
		this.stockId = stockId;
		this.logDate = logDate;
		this.weight = weight;
	}
	
	public ChipInLog() {}
	
	public String getId() {
		this.id = this.stockId + "_" + this.logDate;
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStockId() {
		return stockId;
	}
	public void setStockId(String stockId) {
		this.stockId = stockId;
	}
	public int getLogDate() {
		return logDate;
	}
	public void setLogDate(int logDate) {
		this.logDate = logDate;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	
	
}
