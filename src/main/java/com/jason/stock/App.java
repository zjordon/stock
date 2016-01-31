package com.jason.stock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.CssSelectorNodeFilter;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.util.StringUtils;

import com.jason.stock.http.core.CloseableHttpResponseExtractor;
import com.jason.stock.http.core.DataAccessException;
import com.jason.stock.http.core.DefaultGetHttpDataSource;
import com.jason.stock.http.core.HttpTemplate;
import com.jason.stock.util.DateUtil;
import com.jason.stock.util.StockFileUtil;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	App app = new App();
//    	app.getAllStockes();
//    	app.downloadStock("002646", "sz");
//    	try {
//			app.downloadAllStockes();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	app.parseStockFile();
//    	app.getStockFromSinal();
//    	app.getAllWeek(20141229, 20151204);
    	app.getWorkEndDate(20151221, 10);
    }
    
    public App(){}
    
    public void getAllStockes() {
    	Map<String, String> paramMap = new HashMap<String, String>();
    	DefaultGetHttpDataSource dataSource = new DefaultGetHttpDataSource("http://bbs.10jqka.com.cn/codelist.html", paramMap, 3000);
    	dataSource.init();
    	HttpTemplate httpTemplate = new HttpTemplate(dataSource);
    	String responseContent = httpTemplate.query(new CloseableHttpResponseExtractor<String>(){

			@Override
			public String extractData(CloseableHttpResponse rs) throws IOException,
					DataAccessException {
				HttpEntity entity = rs.getEntity();
				String responsContent = EntityUtils.toString(entity, "gbk");
				return responsContent;
			}
    		
    	});
    	try {
			Parser parser = new Parser(responseContent);
//			RegexFilter filter = new RegexFilter("class=\"bbsilst_wei3\"");
//			TagNameFilter filter = new TagNameFilter("div");
			CssSelectorNodeFilter filter=new CssSelectorNodeFilter ("div[class=\"bbsilst_wei3\"]");
			NodeList nodes = parser.extractAllNodesThatMatch(filter);
			for (NodeIterator i = nodes.elements (); i.hasMoreNodes(); ) {
				Node node = i.nextNode();
//				System.out.println(node.getText());
				NodeList bbsilst_wei3Nodes = node.getChildren();
				for (NodeIterator bbsilst_wei3Node = bbsilst_wei3Nodes.elements (); bbsilst_wei3Node.hasMoreNodes(); ) {
					node = bbsilst_wei3Node.nextNode();
					if (node.getText().contains("id=\"sh\"")) {
						this.findStockMessageNodes(node, "ss");
					} else if (node.getText().contains("id=\"sz\"")) {
						this.findStockMessageNodes(node, "sz");
					}
					
				}
				
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
//        System.out.println( responseContent );
    }
    
    public void findStockMessageNodes(Node node, String type) throws ParserException {
//    	System.out.println(node.getText());
		Node nextNode = node.getNextSibling();
		while (((nextNode=nextNode.getNextSibling()) != null)) {
//			System.out.printf("nextNode is %s\n", nextNode.getText());
			if (nextNode.getText().equals("ul")) {
//				System.out.printf("nextNode is %s\n", nextNode.getText());
				NodeList liNodes = nextNode.getChildren();
				for (NodeIterator liNode = liNodes.elements (); liNode.hasMoreNodes(); ) {
					node = liNode.nextNode();
//					System.out.println(node.getText());
					if (node.getText().equals("li")) {
						NodeList aNodes = node.getChildren();
						for (NodeIterator aNode = aNodes.elements (); aNode.hasMoreNodes(); ) {
							node = aNode.nextNode();
							NodeList messageNodes = node.getChildren();
							for (NodeIterator messageNode = messageNodes.elements (); messageNode.hasMoreNodes(); ) {
								node = messageNode.nextNode();
								System.out.println(node.getText() + " " + type);
							}
							
						}
					}
				}
				break;
			}
		}
    }
    
    public void downloadAllStockes() throws IOException {
    	String errorFilepath = StockFileUtil.STOCK_DATA_PATH + StockFileUtil.SPLASH + "error_log.txt";
    	File errorFile = new File(errorFilepath);
    	if (errorFile.exists()) {
    		errorFile.delete();
    	}
    	FileWriter fileout = new FileWriter(errorFile);
    	String filepath = StockFileUtil.STOCK_DATA_PATH + StockFileUtil.SPLASH + "all_stockes_remain.txt";
		File file = new File(filepath);
		if (file.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String readLine = null;
			String code = null;
			String type = null;
			while ((readLine = br.readLine()) != null) {
//				System.out.println(readLine);
				String[] ss = StockFileUtil.split(readLine);
				code = ss[1];
				type = ss[2];
//				System.out.printf("%s,%s,%s\n", ss[0], ss[1], ss[2]);
//				String[] ss = readLine.split(" ");
//				if (ss.length == 3) {
//					code = ss[1];
//					type = ss[2];
//					
//				} else {
//					for (int i=0; i<ss.length; i++) {
//						if (this.isNumber(ss[i])) {
//							code = ss[i];
//							type = ss[i+1];
//							break;
//						}
//					}
//				}
				try {
					this.downloadStock(code, type);
				} catch (DataAccessException dae) {
					fileout.write((dae.getMessage() + " " + code + " " + type + "\n"));
				}
				
			}
			br.close();
		}
		fileout.flush();  
        fileout.close(); 
    }
    
    public void downloadStock(final String code, final String type) {
    	String filepath = StockFileUtil.STOCK_DATA_PATH + StockFileUtil.SPLASH + type + StockFileUtil.SPLASH + code + ".txt";
		File file = new File(filepath);
		if (file.exists()) {
			System.out.printf("the file is downloaded code is %s type is %s\n", code, type);
			return;
		}
    	System.out.printf("begin down load code is %s type is %s\n", code, type);
    	Map<String, String> paramMap = new HashMap<String, String>();
    	paramMap.put("s", code+"."+type);
    	DefaultGetHttpDataSource dataSource = new DefaultGetHttpDataSource("http://table.finance.yahoo.com/table.csv", paramMap, 10000);
    	dataSource.init();
    	HttpTemplate httpTemplate = new HttpTemplate(dataSource);
    	httpTemplate.query(new CloseableHttpResponseExtractor<String>(){

			@Override
			public String extractData(CloseableHttpResponse rs) throws IOException,
					DataAccessException {
				HttpEntity entity = rs.getEntity();
				if (entity.isStreaming()) {
					InputStream is = entity.getContent();
					String filepath = StockFileUtil.STOCK_DATA_PATH + StockFileUtil.SPLASH + type + StockFileUtil.SPLASH + code + ".txt";
					File file = new File(filepath);
					FileOutputStream fileout = new FileOutputStream(file);
					byte[] buffer=new byte[4096];  
		            int ch = 0;  
		            while ((ch = is.read(buffer)) != -1) {  
		                fileout.write(buffer,0,ch);  
		            }  
		            is.close();  
		            fileout.flush();  
		            fileout.close(); 
				}
				return "";
			}
    		
    	});
    }
    
    public void parseStockFile() {
    	File szDir = new File(StockFileUtil.STOCK_DATA_PATH + StockFileUtil.SPLASH + "test");
		if (szDir.isDirectory()) {
			String[] fileList =  szDir.list();
			for (String fileName : fileList) {
				File file = new File(StockFileUtil.STOCK_DATA_PATH + StockFileUtil.SPLASH + "test" + StockFileUtil.SPLASH + fileName);
				if (file.exists()) {
					String stockCode = fileName.substring(0, fileName.indexOf('.'));
					try {
						BufferedReader br = new BufferedReader(new FileReader(file));
						String readLine = null;
						br.readLine();
						while ((readLine = br.readLine()) != null) {
							String[] ss = readLine.split(",");
							int date = this.formatDateToInt(ss[0]);
							int open = (int)(Double.parseDouble(ss[1]) * 100);
							int high = (int)(Double.parseDouble(ss[2]) * 100);
							int low = (int)(Double.parseDouble(ss[3]) * 100);
							int close = (int)(Double.parseDouble(ss[4]) * 100);
							int volume = ss[5].equals("000") ? 0 : Integer.parseInt(ss[5]);
							System.out.printf("%s,%d,%d,%d,%d,%d,%d\n", stockCode, date, open, high, low, close, volume);
						}
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
    }
    
    public void getStockFromSinal() {
    	Map<String, String> paramMap = new HashMap<String, String>();
//    	DefaultGetHttpDataSource dataSource = new DefaultGetHttpDataSource("http://hq.sinajs.cn/list=sh600755", paramMap, 3000);
    	DefaultGetHttpDataSource dataSource = new DefaultGetHttpDataSource("http://hq.sinajs.cn/list=sz601299", paramMap, 3000);
    	dataSource.init();
    	HttpTemplate httpTemplate = new HttpTemplate(dataSource);
    	String responseContent = httpTemplate.query(new CloseableHttpResponseExtractor<String>(){

			@Override
			public String extractData(CloseableHttpResponse rs) throws IOException,
					DataAccessException {
				HttpEntity entity = rs.getEntity();
				String responsContent = EntityUtils.toString(entity, "gbk");
				System.out.println(responsContent);
//				String[] ss = StringUtils.split(responsContent, ",");
				String[] ss = responsContent.split(",");
				if (ss.length > 31) {
					System.out.printf("%s,%s,%s,%s,%s,%s", ss[30], ss[1], ss[4], ss[5], ss[6], ss[8]);
				}
				
//				System.out.println(ss.length);
//				for (int i=0; i<ss.length;i++) {
//					System.out.println(ss[i]);
//				}
				
				return responsContent;
			}
    		
    	});
    }
    
    private boolean isNumber(String s) {
    	char[] cs = s.toCharArray();
    	for (char c : cs) {
    		if (!Character.isDigit(c)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private int formatDateToInt(String dateStr) {
		int dateInt = 0;
		String[] ss = dateStr.split("-");
		dateInt = Integer.parseInt(ss[0]) * 10000 + Integer.parseInt(ss[1]) * 100 + Integer.parseInt(ss[2]);
		return dateInt;
	}
    
    public void getAllWeek(int startDate, int endDate) {
    	List<int[]> list = DateUtil.getAllWeek(startDate, endDate);
    	for (int[] week : list) {
    		System.out.printf("%d,%d\n", week[0], week[1]);
    	}
//    	Calendar cal = Calendar.getInstance();
//    	System.out.printf("%d,%d,%d\n", cal.get(Calendar.DAY_OF_WEEK), Calendar.MONDAY, Calendar.FRIDAY);
    }
    
    public void getWorkEndDate(int startDate, int dayNum) {
    	int endDate = DateUtil.getWorkEndDate(startDate, dayNum);
    	System.out.println(endDate);
    }
}
