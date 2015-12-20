/**
 * 
 */
package com.jason.stock;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

import com.jason.stock.dao.StockDao;

/**
 * @author lenovo
 *
 */
public class Startup {

	public final static void main(String[] args) {
		DefaultListableBeanFactory beanRegistry = new DefaultListableBeanFactory();
		BeanFactory container = (BeanFactory)bindViaPropertiesFile(beanRegistry);
		StockDao stockDao = (StockDao)container.getBean("stockDao");
//		stockDao.initStock();
//		stockDao.initHisMarket();
//		stockDao.initLatestHisMarket();
//		stockDao.initHisMarketWeek("601939", 20131230, 20151204);
//		stockDao.initHisMarketWeek("002594", 20051128, 20151204);
//		stockDao.initHisMarketWeek(20151214, 20151218);
//		stockDao.initHisMarketWeek(20051128, 20151204);
//		stockDao.trendSum("300051", 20130101, 20151231);
//		stockDao.trendSumWeek("300051", 20051128, 20151231);
//		stockDao.calSomeDay("300051", 20151218, 10, 5);
		stockDao.calSomeWeek("300051", 20151218, 25, 8);
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
