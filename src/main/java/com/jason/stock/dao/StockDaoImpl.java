/**
 * 
 */
package com.jason.stock.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.jason.stock.bean.HisMarket;
import com.jason.stock.http.core.CloseableHttpResponseExtractor;
import com.jason.stock.http.core.DefaultGetHttpDataSource;
import com.jason.stock.http.core.HttpTemplate;
import com.jason.stock.util.DateUtil;
import com.jason.stock.util.StockFileUtil;

/**
 * @author lenovo
 *
 */
public class StockDaoImpl implements StockDao {

	private final Log logger = LogFactory.getLog(getClass());

	private final static String SELECT_COUNT_HIS_MARKET = "select count(1) from st_his_market where stock_id = ?";
	private final static String SELECT_COUNT_HIS_MARKET_WITH_ID = "select count(1) from st_his_market where id = ?";
	private final static String INSERT_HIS_MARKET = "insert into st_his_market(id, stock_id, his_date, open, high, low, close, volume) values(?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String INSERT_HIS_MARKET_WEEK = "insert into st_his_market_week(id, stock_id, his_date, open, high, low, close, volume) values(?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String SELECT_HIS_MARKET_RANGE = "select id, stock_id, his_date, open, close, high, low, volume from st_his_market where stock_id = ? and his_date >= ? and his_date <= ? order by his_date desc";
	private final static String SELECT_HIS_MARKET_RANGE_ASC = "select id, stock_id, his_date, open, close, high, low, volume from st_his_market where stock_id = ? and his_date >= ? and his_date <= ? order by his_date asc";
	private final static String SELECT_HIS_MARKET_WEEK_RANGE = "select id, stock_id, his_date, open, close, high, low, volume from st_his_market_week where stock_id = ? and his_date >= ? and his_date <= ? order by his_date desc";

	private final static String SELECT_COUNT_STOCK = "select count(1) from st_stock where id = ?";
	private final static String INSERT_STOCK = "insert into st_stock(id, type, stock_name) values(?, ?, ?)";
	private final static String SELECT_ALL_STOCK = "select id, type from st_stock";
	private final static String SELECT_ONE_STOCK = "select id, type from st_stock where id = '002646'";

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jason.stock.dao.StockDao#initStock()
	 */
	@Override
	public void initStock() {
		String filepath = StockFileUtil.STOCK_DATA_PATH + StockFileUtil.SPLASH
				+ "all_stockes.txt";
		final File file = new File(filepath);
		if (file.exists()) {
			this.jdbcTemplate.execute(new ConnectionCallback<String>() {

				@Override
				public String doInConnection(Connection con)
						throws SQLException, DataAccessException {
					PreparedStatement pstmt = null;
					BufferedReader br;
					try {
						br = new BufferedReader(new FileReader(file));
						String readLine = null;
						String name = null;
						String code = null;
						String type = null;
						while ((readLine = br.readLine()) != null) {
							String[] ss = StockFileUtil.split(readLine);
							name = ss[0];
							code = ss[1];
							type = ss[2];
							pstmt = con.prepareStatement(SELECT_COUNT_STOCK);
							pstmt.setString(1, code);
							ResultSet rs = pstmt.executeQuery();
							rs.next();
							int count = rs.getInt(1);
							rs.close();
							pstmt.close();
							if (count == 0) {
								logger.info(readLine);
								pstmt = con.prepareStatement(INSERT_STOCK);
								pstmt.setString(1, code);
								pstmt.setString(2, type);
								pstmt.setString(3, name);
								pstmt.executeUpdate();
								pstmt.close();
							} else {
								logger.info("the record is exists with code "
										+ code);
							}
						}
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					con.commit();
					return null;
				}

			});

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jason.stock.dao.StockDao#initHisMarket()
	 */
	@Override
	public void initHisMarket() {

		this.jdbcTemplate.execute(new ConnectionCallback<String>() {

			@Override
			public String doInConnection(Connection con) throws SQLException,
					DataAccessException {
				// StockDaoImpl.this.initHisMarket("sz", con);
				// StockDaoImpl.this.initHisMarket("ss", con);
				StockDaoImpl.this.initHisMarket("test", con);
				return null;
			}

		});

	}

	private void initHisMarket(String rootDir, Connection con)
			throws SQLException {
		File szDir = new File(StockFileUtil.STOCK_DATA_PATH
				+ StockFileUtil.SPLASH + rootDir);
		if (szDir.isDirectory()) {
			PreparedStatement pstmt = null;
			String[] fileList = szDir.list();
			for (String fileName : fileList) {
				File file = new File(szDir.getAbsolutePath()
						+ StockFileUtil.SPLASH + fileName);
				if (file.exists()) {
					String stockCode = fileName.substring(0,
							fileName.indexOf('.'));
					pstmt = con.prepareStatement(SELECT_COUNT_HIS_MARKET);
					pstmt.setString(1, stockCode);
					ResultSet rs = pstmt.executeQuery();
					rs.next();
					int count = rs.getInt(1);
					rs.close();
					pstmt.close();
					if (count > 1) {
						logger.info("these his market records is exists with code "
								+ stockCode);
						continue;
					}
					pstmt = con.prepareStatement(INSERT_HIS_MARKET);
					try {
						BufferedReader br = new BufferedReader(new FileReader(
								file));
						String readLine = null;
						br.readLine();
						while ((readLine = br.readLine()) != null) {
							String[] ss = readLine.split(",");
							this.insertHisMarket(pstmt, stockCode, ss);
							// pstmt.addBatch();
						}
						pstmt.executeBatch();
						pstmt.close();
						con.commit();
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	private void insertHisMarket(PreparedStatement pstmt, String stockCode,
			String[] ss) throws SQLException {
		if (ss.length > 5) {
			String id = stockCode + ss[0];
			int date = StockDaoImpl.this.formatDateToInt(ss[0]);
			int open = (int) (Double.parseDouble(ss[1]) * 100);
			int high = (int) (Double.parseDouble(ss[2]) * 100);
			int low = (int) (Double.parseDouble(ss[3]) * 100);
			int close = (int) (Double.parseDouble(ss[4]) * 100);
			long volume = ss[5].equals("000") ? 0 : Long.parseLong(ss[5]);
			System.out.printf("%s,%s,%d,%d,%d,%d,%d,%d\n", id, stockCode, date,
					open, high, low, close, volume);
			pstmt.setString(1, id);
			pstmt.setString(2, stockCode);
			pstmt.setInt(3, date);
			pstmt.setInt(4, open);
			pstmt.setInt(5, high);
			pstmt.setInt(6, low);
			pstmt.setInt(7, close);
			pstmt.setLong(8, volume);
			// pstmt.executeUpdate();
			pstmt.addBatch();
		} else {
			logger.warn("this ss length is not > 5 with " + ss.length);
		}

	}

	private int formatDateToInt(String dateStr) {
		int dateInt = 0;
		String[] ss = dateStr.split("-");
		dateInt = Integer.parseInt(ss[0]) * 10000 + Integer.parseInt(ss[1])
				* 100 + Integer.parseInt(ss[2]);
		return dateInt;
	}

	@Override
	public void initLatestHisMarket() {
		this.jdbcTemplate.execute(new ConnectionCallback<String>() {

			@Override
			public String doInConnection(Connection con) throws SQLException,
					DataAccessException {
				 PreparedStatement pstmt =
				 con.prepareStatement(SELECT_ALL_STOCK);
//				PreparedStatement pstmt = con
//						.prepareStatement(SELECT_ONE_STOCK);
				ResultSet rs = pstmt.executeQuery();
				List<String[]> stockes = new ArrayList<String[]>();
				while (rs.next()) {
					String[] stock = new String[] { rs.getString(1),
							rs.getString(2) };
					stockes.add(stock);
				}
				rs.close();
				pstmt.close();
				if (!stockes.isEmpty()) {
					pstmt = con.prepareStatement(INSERT_HIS_MARKET);
					PreparedStatement queryPstmt = con
							.prepareStatement(SELECT_COUNT_HIS_MARKET_WITH_ID);
					for (String[] stock : stockes) {
						String[] hisMarket = null;
						try {
							hisMarket = StockDaoImpl.this.getStockFromSinal(
									stock[1], stock[0]);
						} catch (DataAccessException dae) {
							logger.error(dae.getMessage() + "," + stock[0]
									+ "," + stock[1]);
						}
						if (hisMarket != null) {
							queryPstmt.setString(1, stock[0] + hisMarket[0]);
							rs = queryPstmt.executeQuery();
							rs.next();
							int count = rs.getInt(1);
							rs.close();
							if (count == 0) {
								StockDaoImpl.this.insertHisMarket(pstmt,
										stock[0], hisMarket);
								// pstmt.executeUpdate();
								// pstmt.addBatch();
							} else {
								logger.info("his market record is existed with id "
										+ (stock[0] + hisMarket[0]));
							}

						}
					}
					pstmt.executeBatch();
					queryPstmt.close();
					pstmt.close();
					con.commit();
				}
				return null;
			}

		});

	}

	private final String[] getStockFromSinal(final String type,
			final String stockCode) {
		Map<String, String> paramMap = new HashMap<String, String>();
		String paramValue = ("ss".equals(type) ? "sh" : type) + stockCode;
		// DefaultGetHttpDataSource dataSource = new
		// DefaultGetHttpDataSource("http://hq.sinajs.cn/list=sh600755",
		// paramMap, 3000);
		DefaultGetHttpDataSource dataSource = new DefaultGetHttpDataSource(
				"http://hq.sinajs.cn/list=" + paramValue, paramMap, 3000);
		dataSource.init();
		HttpTemplate httpTemplate = new HttpTemplate(dataSource);
		return httpTemplate
				.query(new CloseableHttpResponseExtractor<String[]>() {

					@Override
					public String[] extractData(CloseableHttpResponse rs)
							throws IOException, DataAccessException {
						HttpEntity entity = rs.getEntity();
						String responsContent = EntityUtils.toString(entity,
								"gbk");
						// System.out.println(responsContent);
						String[] ss = responsContent.split(",");
						String[] hisMarket = null;
						if (ss.length > 31) {
							hisMarket = new String[6];
							// System.out.printf("%s,%s,%s,%s,%s,%s\n", ss[30],
							// ss[1], ss[4], ss[5], ss[6], ss[8]);
							hisMarket[0] = ss[30];
							hisMarket[1] = ss[1];
							hisMarket[2] = ss[4];
							hisMarket[3] = ss[5];
							hisMarket[4] = ss[6];
							hisMarket[5] = ss[8];
						} else {
							logger.info("no record with stockCode " + stockCode);
						}

						return hisMarket;
					}

				});
	}

	@Override
	public void trendSum(final String stockId, final long startTime,
			final long endTime) {
		ArrayList<HisMarket> list = this.jdbcTemplate.query(
				SELECT_HIS_MARKET_RANGE, new Object[] { stockId,
						new Long(startTime), new Long(endTime) },
				new ResultSetExtractor<ArrayList<HisMarket>>() {

					@Override
					public ArrayList<HisMarket> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						return StockDaoImpl.this.getHisMarketList(rs);
					}

				});
		if (!list.isEmpty()) {
			// 一阳包一阴
//			this.processOne(list, 10, 5);
			//一阴后一阳，并且阳线跳空高开
//			this.processTwelve(list, 10, 5);
//			一阴后一阳，并且阳线高于阴线
			this.processFourteen(list, 10, 5);
			// 一阳包一阴，一阴是十字星
//			this.processSix(list, 10, 5, 0.125);
			// 三连阳
//			 this.processTwo(list, 10, 5);
			// 四连阳
//			 this.processTen(list, 10, 5);
			// 二连阳
//			 this.processThree(list, 10, 5);
			 //二连阳 跳空高开
//			 processEleven(list, 10, 5);
			// 二连阳 并且是 两个十字星
//			processSeven(list, 10, 5, 0.275);
			//十字星
			this.processFour(list, 10, 5, 0.276);
			//十字星后一个阳线
//			this.processThirteen(list, 10, 5, 0.265);
			//连续两个十字星
//			processNine(list, 10, 5, 0.276);
			//连续三个十字星
//			processEight(list, 10, 5, 0.276);
//			 二连阳 一阳包一阳
//			 processFive(list, 10, 5);
		} else {
			logger.info("list is empty");
		}

	}

	@Override
	public void trendSumWeek(final String stockId, final long startTime,
			final long endTime) {
		ArrayList<HisMarket> list = this.jdbcTemplate.query(
				SELECT_HIS_MARKET_WEEK_RANGE, new Object[] { stockId,
						new Long(startTime), new Long(endTime) },
				new ResultSetExtractor<ArrayList<HisMarket>>() {

					@Override
					public ArrayList<HisMarket> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						return StockDaoImpl.this.getHisMarketList(rs);
					}

				});
		if (!list.isEmpty()) {
			// 一阳包一阴
//			 this.processOne(list, 5, 8);
			// 一阳包一阴，一阴是十字星
//				this.processSix(list, 5, 8, 0.125);
			// 一阳包一阴，一阴是十字星
//				this.processSix(list, 5, 8, 0.125);
			// 三连阳
//			this.processTwo(list, 5, 8);
			// 二连阳
			 this.processThree(list, 5, 8);
			//十字星
//			this.processFour(list, 5, 8, 0.276);
//			 二连阳 一阳包一阳
//			 processFive(list, 5, 8);
		} else {
			logger.info("list is empty");
		}

	}

	private ArrayList<HisMarket> getHisMarketList(ResultSet rs)
			throws SQLException {
		ArrayList<HisMarket> hisMarkets = new ArrayList<HisMarket>();
		while (rs.next()) {
			HisMarket market = new HisMarket();
			market.setId(rs.getString(1));
			market.setStockId(rs.getString(2));
			market.setHisDate(rs.getInt(3));
			market.setOpen(rs.getInt(4));
			market.setClose(rs.getInt(5));
			market.setHigh(rs.getInt(6));
			market.setLow(rs.getInt(7));
			market.setVolume(rs.getLong(8));
			hisMarkets.add(market);
		}
		return hisMarkets;
	}

	private void processOne(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int nextDayRange = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			// logger.info("currentDayRange is " + currentDayRange);
			if (currentDayRange > 0) {
				nextDayRange = list.get(i + 1).getRange();
				// logger.info("currentDayRange is " + currentDayRange +
				// " nextDayRange is " + nextDayRange);
				if (nextDayRange < 0) {
					if (currentDayRange - (nextDayRange - (nextDayRange * 2)) >= 0) {
						// logger.info(list.get(i).getHisDate());
						System.out.println(list.get(i).getHisDate());
						int start = i + 1;
						int end = i + endNum;
						// for (int j=(start+1); j<end && j<size; j++) {
						// double preClose = (double)list.get(j).getClose();
						// double curClose = (double)list.get(start).getClose();
						// if (((preClose - curClose)/preClose) * 100 > 10) {
						// System.out.println("down yes");
						// break;
						// }
						// }
						start = i;
						end = i - 10;
						for (int j = start; j > end && j >= 0; j--) {
							double preClose = (double) list.get(j).getClose();
							double curClose = (double) list.get(start)
									.getClose();
							// System.out.printf("%d,%d,%d,%d\n",
							// list.get(j).getClose(),
							// list.get(start).getClose(),
							// (list.get(j).getClose() -
							// list.get(start).getClose()),((list.get(j).getClose()
							// -
							// list.get(start).getClose())/list.get(j).getClose()));
							if (((preClose - curClose) / preClose) * 100 > rose) {
								System.out.println("up yes");
								break;
							}
						}
					}
				}
			}
		}
	}

	private void processTwo(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int theDayBeforeYesterdayRange = 0;
		int yesterdayRange = 0;
		for (int i = 0; i + 2 < size; i++) {
			currentDayRange = list.get(i).getRange();
			yesterdayRange = list.get(i + 1).getRange();
			theDayBeforeYesterdayRange = list.get(i + 2).getRange();
			if (currentDayRange > 0 && yesterdayRange > 0
					&& theDayBeforeYesterdayRange > 0) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}

	private void processThree(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int yesterdayRange = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			yesterdayRange = list.get(i + 1).getRange();
			if (currentDayRange > 0 && yesterdayRange > 0) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}

	private void processFour(ArrayList<HisMarket> list, int endNum, int rose,
			double limit) {
		int size = list.size();
		logger.info("list size is " + size);
		for (int i = 0; i + 1 < size; i++) {
			double limitPercent = list.get(i).getLimitPercent();
//			System.out.printf("%d,%f\n", list.get(i).getHisDate(),limitPercent);
			if (limitPercent <= limit) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}
	
	
	private void processThirteen(ArrayList<HisMarket> list, int endNum, int rose,
			double limit) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			if (currentDayRange > 0) {
				double limitPercent = list.get(i + 1).getLimitPercent();
//				System.out.printf("%d,%f\n", list.get(i).getHisDate(),limitPercent);
				if (limitPercent <= limit) {
					System.out.println(list.get(i).getHisDate());
					int start = i;
					int end = i - endNum;
					for (int j = start; j > end && j >= 0; j--) {
						double preClose = (double) list.get(j).getClose();
						double curClose = (double) list.get(start).getClose();
						// System.out.printf("%d,%d,%d,%d\n",
						// list.get(j).getClose(), list.get(start).getClose(),
						// (list.get(j).getClose() -
						// list.get(start).getClose()),((list.get(j).getClose() -
						// list.get(start).getClose())/list.get(j).getClose()));
						if (((preClose - curClose) / preClose) * 100 > rose) {
							System.out.println("up yes");
							break;
						}
					}
				}
			}
			
		}
	}
	
	private void processFive(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int yesterdayRange = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			yesterdayRange = list.get(i + 1).getRange();
			if (currentDayRange > 0 && yesterdayRange > 0 && currentDayRange > yesterdayRange) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}
	
	private void processSix(ArrayList<HisMarket> list, int endNum, int rose, double limit) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int nextDayRange = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			// logger.info("currentDayRange is " + currentDayRange);
			if (currentDayRange > 0) {
				nextDayRange = list.get(i + 1).getRange();
				// logger.info("currentDayRange is " + currentDayRange +
				// " nextDayRange is " + nextDayRange);
				if (nextDayRange < 0) {
					double limitPercent = list.get(i + 1).getLimitPercent();
//					System.out.printf("%d, %f\n", list.get(i).getHisDate(), limitPercent);
					if (currentDayRange - (nextDayRange - (nextDayRange * 2)) >= 0 && limitPercent <= limit) {
//						 logger.info(list.get(i).getHisDate());
						System.out.println(list.get(i).getHisDate());
						int start = i + 1;
						int end = i + endNum;
						// for (int j=(start+1); j<end && j<size; j++) {
						// double preClose = (double)list.get(j).getClose();
						// double curClose = (double)list.get(start).getClose();
						// if (((preClose - curClose)/preClose) * 100 > 10) {
						// System.out.println("down yes");
						// break;
						// }
						// }
						start = i;
						end = i - 10;
						for (int j = start; j > end && j >= 0; j--) {
							double preClose = (double) list.get(j).getClose();
							double curClose = (double) list.get(start)
									.getClose();
							// System.out.printf("%d,%d,%d,%d\n",
							// list.get(j).getClose(),
							// list.get(start).getClose(),
							// (list.get(j).getClose() -
							// list.get(start).getClose()),((list.get(j).getClose()
							// -
							// list.get(start).getClose())/list.get(j).getClose()));
							if (((preClose - curClose) / preClose) * 100 > rose) {
								System.out.println("up yes");
								break;
							}
						}
					}
				}
			}
		}
	}
	
	private void processSeven(ArrayList<HisMarket> list, int endNum, int rose, double limit) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int yesterdayRange = 0;
		double currentDayLimit = 0.0;
		double yesterdayLimit = 0.0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			yesterdayRange = list.get(i + 1).getRange();
			currentDayLimit = list.get(i).getLimitPercent();
			yesterdayLimit = list.get(i+1).getLimitPercent();
			if (currentDayRange >= 0 && yesterdayRange >= 0 && currentDayLimit <= limit && yesterdayLimit <= limit) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}
	
	private void processEight(ArrayList<HisMarket> list, int endNum, int rose,
			double limit) {
		int size = list.size();
		logger.info("list size is " + size);
		for (int i = 0; i + 2 < size; i++) {
			double limitPercent = list.get(i).getLimitPercent();
			double yesterdaylimitPercent = list.get(i + 1).getLimitPercent();
			double tdbylimitPercent = list.get(i + 2).getLimitPercent();
//			System.out.printf("%d,%f\n", list.get(i).getHisDate(),limitPercent);
			if (limitPercent <= limit && yesterdaylimitPercent <= limit && tdbylimitPercent <= limit) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}
	
	private void processNine(ArrayList<HisMarket> list, int endNum, int rose,
			double limit) {
		int size = list.size();
		logger.info("list size is " + size);
		for (int i = 0; i + 1 < size; i++) {
			double limitPercent = list.get(i).getLimitPercent();
			double yesterdaylimitPercent = list.get(i + 1).getLimitPercent();
//			System.out.printf("%d,%f\n", list.get(i).getHisDate(),limitPercent);
			if (limitPercent <= limit && yesterdaylimitPercent <= limit) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}
	
	private void processTen(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int theDayBeforeYesterdayRange = 0;
		int yesterdayRange = 0;
		int fourDayRange = 0;
		for (int i = 0; i + 3 < size; i++) {
			currentDayRange = list.get(i).getRange();
			yesterdayRange = list.get(i + 1).getRange();
			theDayBeforeYesterdayRange = list.get(i + 2).getRange();
			fourDayRange = list.get(i + 3).getRange();
			if (currentDayRange > 0 && yesterdayRange > 0
					&& theDayBeforeYesterdayRange > 0 && fourDayRange > 0) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					// System.out.printf("%d,%d,%d,%d\n",
					// list.get(j).getClose(), list.get(start).getClose(),
					// (list.get(j).getClose() -
					// list.get(start).getClose()),((list.get(j).getClose() -
					// list.get(start).getClose())/list.get(j).getClose()));
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}
	
	private void processEleven(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int yesterdayRange = 0;
		int currentDayOpen = 0;
		int yesterdayHigh = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			currentDayOpen = list.get(i).getOpen();
			yesterdayRange = list.get(i + 1).getRange();
			yesterdayHigh = list.get(i + 1).getHigh();
			if (currentDayRange > 0 && yesterdayRange > 0 && currentDayOpen > yesterdayHigh) {
				System.out.println(list.get(i).getHisDate());
				int start = i;
				int end = i - endNum;
				for (int j = start; j > end && j >= 0; j--) {
					double preClose = (double) list.get(j).getClose();
					double curClose = (double) list.get(start).getClose();
					if (((preClose - curClose) / preClose) * 100 > rose) {
						System.out.println("up yes");
						break;
					}
				}
			}
		}
	}
	
	private void processTwelve(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int nextDayRange = 0;
		int currentOpen = 0;
		int nextDayOpen = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			currentOpen = list.get(i).getOpen();
			// logger.info("currentDayRange is " + currentDayRange);
			if (currentDayRange > 0) {
				nextDayRange = list.get(i + 1).getRange();
				nextDayOpen = list.get(i+ 1).getOpen();
				// logger.info("currentDayRange is " + currentDayRange +
				// " nextDayRange is " + nextDayRange);
				if (nextDayRange < 0) {
					if (currentOpen >= nextDayOpen) {
						// logger.info(list.get(i).getHisDate());
						System.out.println(list.get(i).getHisDate());
						int start = i + 1;
						int end = i + endNum;
						// for (int j=(start+1); j<end && j<size; j++) {
						// double preClose = (double)list.get(j).getClose();
						// double curClose = (double)list.get(start).getClose();
						// if (((preClose - curClose)/preClose) * 100 > 10) {
						// System.out.println("down yes");
						// break;
						// }
						// }
						start = i;
						end = i - 10;
						for (int j = start; j > end && j >= 0; j--) {
							double preClose = (double) list.get(j).getClose();
							double curClose = (double) list.get(start)
									.getClose();
							// System.out.printf("%d,%d,%d,%d\n",
							// list.get(j).getClose(),
							// list.get(start).getClose(),
							// (list.get(j).getClose() -
							// list.get(start).getClose()),((list.get(j).getClose()
							// -
							// list.get(start).getClose())/list.get(j).getClose()));
							if (((preClose - curClose) / preClose) * 100 > rose) {
								System.out.println("up yes");
								break;
							}
						}
					}
				}
			}
		}
	}
	
	private void processFourteen(ArrayList<HisMarket> list, int endNum, int rose) {
		int size = list.size();
		logger.info("list size is " + size);
		int currentDayRange = 0;
		int nextDayRange = 0;
		int currentClose = 0;
		int nextDayOpen = 0;
		for (int i = 0; i + 1 < size; i++) {
			currentDayRange = list.get(i).getRange();
			currentClose = list.get(i).getClose();
			// logger.info("currentDayRange is " + currentDayRange);
			if (currentDayRange > 0) {
				nextDayRange = list.get(i + 1).getRange();
				nextDayOpen = list.get(i+ 1).getOpen();
				// logger.info("currentDayRange is " + currentDayRange +
				// " nextDayRange is " + nextDayRange);
				if (nextDayRange < 0) {
					if (currentClose >= nextDayOpen) {
						// logger.info(list.get(i).getHisDate());
						System.out.println(list.get(i).getHisDate());
						int start = i + 1;
						int end = i + endNum;
						// for (int j=(start+1); j<end && j<size; j++) {
						// double preClose = (double)list.get(j).getClose();
						// double curClose = (double)list.get(start).getClose();
						// if (((preClose - curClose)/preClose) * 100 > 10) {
						// System.out.println("down yes");
						// break;
						// }
						// }
						start = i;
						end = i - 10;
						for (int j = start; j > end && j >= 0; j--) {
							double preClose = (double) list.get(j).getClose();
							double curClose = (double) list.get(start)
									.getClose();
							// System.out.printf("%d,%d,%d,%d\n",
							// list.get(j).getClose(),
							// list.get(start).getClose(),
							// (list.get(j).getClose() -
							// list.get(start).getClose()),((list.get(j).getClose()
							// -
							// list.get(start).getClose())/list.get(j).getClose()));
							if (((preClose - curClose) / preClose) * 100 > rose) {
								System.out.println("up yes");
								break;
							}
						}
					}
				}
			}
		}
	}
	
	

	@Override
	public void initHisMarketWeek(final String stockId, final int startDate,
			final int endDate) {
		this.jdbcTemplate.execute(new ConnectionCallback<String>() {

			@Override
			public String doInConnection(Connection con) throws SQLException,
					DataAccessException {
				StockDaoImpl.this.initHisMarketWeek(stockId, startDate,
						endDate, con);
				return null;
			}

		});

	}

	@Override
	public void initHisMarketWeek(final int startDate, final int endDate) {
		this.jdbcTemplate.execute(new ConnectionCallback<String>() {

			@Override
			public String doInConnection(Connection con) throws SQLException,
					DataAccessException {
				PreparedStatement pstmt = con
						.prepareStatement(SELECT_ALL_STOCK);
				// PreparedStatement pstmt =
				// con.prepareStatement(SELECT_ONE_STOCK);
				ResultSet rs = pstmt.executeQuery();
				List<String> stockes = new ArrayList<String>();
				while (rs.next()) {
					String stock = rs.getString(1);
					stockes.add(stock);
				}
				rs.close();
				pstmt.close();
				if (!stockes.isEmpty()) {
					for (String stockId : stockes) {
						logger.info("initHisMarketWeek with stockId is "
								+ stockId);
						StockDaoImpl.this.initHisMarketWeek(stockId, startDate,
								endDate, con);
					}
				}
				return null;
			}
		});
	}

	private void initHisMarketWeek(final String stockId, final int startDate,
			final int endDate, Connection con) throws SQLException {
		PreparedStatement pstmt = con
				.prepareStatement(SELECT_HIS_MARKET_RANGE_ASC);
		pstmt.setString(1, stockId);
		pstmt.setInt(2, startDate);
		pstmt.setInt(3, endDate);
		ResultSet rs = pstmt.executeQuery();
		ArrayList<HisMarket> list = StockDaoImpl.this.getHisMarketList(rs);
		rs.close();
		pstmt.close();
		if (!list.isEmpty()) {
			List<int[]> weeks = DateUtil.getAllWeek(startDate, endDate);
			if (!weeks.isEmpty()) {
				ArrayList<HisMarket> insertList = new ArrayList<HisMarket>(
						weeks.size());
				int startIdx = 0;

				for (int[] week : weeks) {
					ArrayList<HisMarket> tempList = new ArrayList<HisMarket>(5);
					for (int i = startIdx; i < list.size(); i++) {
						HisMarket temp = list.get(i);
						if (temp.getHisDate() >= week[0]
								&& temp.getHisDate() <= week[1]) {
							tempList.add(temp);
						} else if (temp.getHisDate() > week[1]) {
							HisMarket hisMarketWeek = this
									.getHisMarketWeek(tempList);
							if (hisMarketWeek != null) {
								insertList.add(hisMarketWeek);
								tempList.clear();
							}
							startIdx = i;
							break;
						}
					}
					if (!tempList.isEmpty()) {
						HisMarket hisMarketWeek = this
								.getHisMarketWeek(tempList);
						if (hisMarketWeek != null) {
							insertList.add(hisMarketWeek);
							tempList.clear();
						}
					}
				}
				if (!insertList.isEmpty()) {
					logger.info("insertList size is " + insertList.size());
					pstmt = con.prepareStatement(INSERT_HIS_MARKET_WEEK);
					for (HisMarket hisMarket : insertList) {
						// System.out.printf("id:%s,open:%d,high:%d,low:%d,close:%d,volume:%d,stockId:%s,hisDate:%d\n",
						// hisMarket.getId(), hisMarket.getOpen(),
						// hisMarket.getHigh(), hisMarket.getLow(),
						// hisMarket.getClose(), hisMarket.getVolume(),
						// hisMarket.getStockId(), hisMarket.getHisDate());
						if (hisMarket.getId() != null) {
							pstmt.setString(1, hisMarket.getId());
							pstmt.setString(2, hisMarket.getStockId());
							pstmt.setInt(3, hisMarket.getHisDate());
							pstmt.setInt(4, hisMarket.getOpen());
							pstmt.setInt(5, hisMarket.getHigh());
							pstmt.setInt(6, hisMarket.getLow());
							pstmt.setInt(7, hisMarket.getClose());
							pstmt.setLong(8, hisMarket.getVolume());
							pstmt.addBatch();
						} else {
							logger.warn(hisMarket.toString());
						}

					}
					pstmt.executeBatch();
					pstmt.close();
					con.commit();
				}
			}
		}
	}

	private HisMarket getHisMarketWeek(ArrayList<HisMarket> tempList) {
		HisMarket hisMarketWeek = null;
		if (!tempList.isEmpty()) {
			hisMarketWeek = new HisMarket();
			long volume = 0;
			int low = 0;
			int high = 0;
			for (int i = 0; i < tempList.size(); i++) {
				HisMarket temp = tempList.get(i);
				if (temp.getHigh() > high) {
					high = temp.getHigh();
				}
				if (temp.getLow() < low || (low == 0)) {
					low = temp.getLow();
				}
				volume += temp.getVolume();
				if (i == 0) {
					hisMarketWeek.setId(temp.getStockId() + temp.getHisDate());
					hisMarketWeek.setOpen(temp.getOpen());
					hisMarketWeek.setStockId(temp.getStockId());
				} else if (i == (tempList.size() - 1)) {
					hisMarketWeek.setClose(temp.getClose());
					hisMarketWeek.setHisDate(temp.getHisDate());
				}
			}
			hisMarketWeek.setHigh(high);
			hisMarketWeek.setLow(low);
			hisMarketWeek.setVolume(volume);
		}

		return hisMarketWeek;
	}

	@Override
	public void calSomeDay(final String stockId, final int endTime, int dayNum, int rose) {
		final int startTime = DateUtil.getStartDate(endTime, dayNum);
		System.out.println(startTime);
		ArrayList<HisMarket> list = this.jdbcTemplate.query(
				SELECT_HIS_MARKET_RANGE, new Object[] { stockId,
						new Long(startTime), new Long(endTime) },
				new ResultSetExtractor<ArrayList<HisMarket>>() {

					@Override
					public ArrayList<HisMarket> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						return StockDaoImpl.this.getHisMarketList(rs);
					}

				});
		if (!list.isEmpty()) {
			int start = list.size() - 1;
			for (int i = start; i>=0; i--) {
				double preClose = (double) list.get(i).getClose();
				double curClose = (double) list.get(start).getClose();
				// System.out.printf("%d,%d,%d,%d\n",
				// list.get(j).getClose(), list.get(start).getClose(),
				// (list.get(j).getClose() -
				// list.get(start).getClose()),((list.get(j).getClose() -
				// list.get(start).getClose())/list.get(j).getClose()));
				if (((preClose - curClose) / preClose) * 100 > rose) {
					System.out.println("up yes");
					break;
				}
			}
		}
		
	}

	@Override
	public void calSomeWeek(String stockId, int endTime, int dayNum, int rose) {
		final int startTime = DateUtil.getStartDate(endTime, dayNum);
		System.out.println(startTime);
		ArrayList<HisMarket> list = this.jdbcTemplate.query(
				SELECT_HIS_MARKET_WEEK_RANGE, new Object[] { stockId,
						new Long(startTime), new Long(endTime) },
				new ResultSetExtractor<ArrayList<HisMarket>>() {

					@Override
					public ArrayList<HisMarket> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						return StockDaoImpl.this.getHisMarketList(rs);
					}

				});
		if (!list.isEmpty()) {
			int start = list.size() - 1;
			for (int i = start; i>=0; i--) {
				double preClose = (double) list.get(i).getClose();
				double curClose = (double) list.get(start).getClose();
				// System.out.printf("%d,%d,%d,%d\n",
				// list.get(j).getClose(), list.get(start).getClose(),
				// (list.get(j).getClose() -
				// list.get(start).getClose()),((list.get(j).getClose() -
				// list.get(start).getClose())/list.get(j).getClose()));
				if (((preClose - curClose) / preClose) * 100 > rose) {
					System.out.println("up yes");
					break;
				}
			}
		}
		
	}

}
