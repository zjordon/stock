/**
 * 
 */
package com.jason.stock.bean;

/**
 * @author jasonzhang
 *
 */
public class HisMarket {

	private String id;
	private int hisDate;
	private int open;
	private int close;
	private int high;
	private int low;
	private long volume;
	private String stockId;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getHisDate() {
		return hisDate;
	}
	public void setHisDate(int hisDate) {
		this.hisDate = hisDate;
	}
	public int getOpen() {
		return open;
	}
	public void setOpen(int open) {
		this.open = open;
	}
	public int getClose() {
		return close;
	}
	public void setClose(int close) {
		this.close = close;
	}
	public int getHigh() {
		return high;
	}
	public void setHigh(int high) {
		this.high = high;
	}
	public int getLow() {
		return low;
	}
	public void setLow(int low) {
		this.low = low;
	}
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}
	public String getStockId() {
		return stockId;
	}
	public void setStockId(String stockId) {
		this.stockId = stockId;
	}
	
	public int getRange() {
		return (this.close - this.open);
	}
	
	public double getLimitPercent() {
		double d1 = this.high - this.low;
		double d2 = (this.close >= this.open) ? (this.close - this.open) : (this.open - this.close);
		return d2/d1;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id:").append(this.id).append(",open:").append(this.open).append(",high:").append(this.high).append(",low:").append(this.low).append(",close:").append(this.close);
		builder.append(",volume:").append(this.volume).append(",stockId:").append(this.stockId).append(",hisDate:").append(this.hisDate);
		return builder.toString();
	}
	
}
