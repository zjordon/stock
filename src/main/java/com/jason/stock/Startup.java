/**
 * 
 */
package com.jason.stock;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.util.StringUtils;

import com.jason.stock.bean.ChipInLog;
import com.jason.stock.dao.StockDao;

/**
 * @author jasonzhang
 *
 */
public class Startup {

	public final static void main(String[] args) {
		DefaultListableBeanFactory beanRegistry = new DefaultListableBeanFactory();
		BeanFactory container = (BeanFactory)bindViaPropertiesFile(beanRegistry);
		StockDao stockDao = (StockDao)container.getBean("stockDao");
//		stockDao.initStock();
//		stockDao.initHisMarket();
		stockDao.initLatestHisMarket();
//		stockDao.initHisMarketWeek("601939", 20131230, 20151204);
//		stockDao.initHisMarketWeek("002594", 20051128, 20151204);
//		stockDao.initHisMarketWeek(20151221, 20151225);
//		stockDao.initHisMarketWeek(20151228, 20160101);
//		stockDao.trendSum("300051", 20140101, 20161231);
//		stockDao.trendSumWeek("300051", 20051128, 20151231);
//		stockDao.calSomeDay("300051", 20160125, 10, 5);
//		stockDao.calSomeWeek("300051", 20151218, 25, 8);
//		stockDao.saveChipInLog("000501", 20151211, "10|10|10");
//		saveChipInLogs(stockDao);
//		stockDao.processUndoChipInLog();
	}
	
	private static void saveChipInLogs(StockDao stockDao) {
		String[] logs = new String[]{"20160126,000100,10",
				"20160126,000755,10",
				"20160126,600320,10",
				"20160126,600410,10|5",
				"20160126,000501,10|5",
				"20160126,600000,10",
				"20160126,601901,10",
				"20160126,002029,10|5",
				"20160126,300051,10|10|5"};
		List<ChipInLog> list = new ArrayList<ChipInLog>(logs.length);
		for (String log : logs) {
			String[] ss = log.split(",");
//			for (String s : ss) {
//				System.out.print(s + " ");
//			}
//			System.out.println();
			ChipInLog chipInLog = new ChipInLog(ss[1], Integer.parseInt(ss[0]), ss[2]);
			list.add(chipInLog);
		}
		if (!list.isEmpty()) {
			stockDao.saveChipInLogs(list);
		}
	}

	private static BeanFactory bindViaPropertiesFile(
			BeanDefinitionRegistry registry) {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
		reader.loadBeanDefinitions("classpath:config.xml");
		return (BeanFactory) registry;
		// 或直接但这个方法在spring3.1版本后已经被不推荐使用
		// return new XmlBeanFactory(new ClassPathResource("news-config.xml"));
	}
}
