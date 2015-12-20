/**
 * 
 */
package com.jason.stock.dao;

/**
 * @author jasonzhang
 *
 */
public interface StockDao {

	void initStock();
	
	void initHisMarket();
	
	void initLatestHisMarket();
	
	void trendSum(String stockId, long startTime, long endTime);
	
	void trendSumWeek(String stockId, long startTime, long endTime);
	
	void initHisMarketWeek(String stockId, int startDate, int endDate);
	
	void initHisMarketWeek(int startDate, int endDate);
	
	void calSomeDay(String stockId, int endTime, int dayNum, int rose);
	
	void calSomeWeek(String stockId, int endTime, int dayNum, int rose);
}
