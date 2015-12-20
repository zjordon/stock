/**
 * 
 */
package com.jason.stock.util;

/**
 * @author jasonzhang
 *
 */
public class StockFileUtil {
	
	public final static String STOCK_DATA_PATH = "D:\\data\\stock";
	public final static String SPLASH = "\\";

	public static String[] split(String s) {
    	String[] ss = new String[3];
    	char[] cs = s.toCharArray();
    	StringBuilder sb = new StringBuilder();
    	boolean isNumber = false;
    	boolean isType = false;
    	for (char c : cs) {
    		if (c != ' ' && !Character.isDigit(c)) {
    			sb.append(c);
    		} else if (Character.isDigit(c)) {
    			if (!isNumber) {
    				isNumber = true;
    				ss[0] = sb.toString();
    				sb.delete(0, sb.length());
    				sb.append(c);
    			} else {
    				sb.append(c);
    			}
    		} else if (c == ' ') {
    			if (isNumber) {
    				isNumber = false;
    				isType = true;
    				ss[1] = sb.toString();
    				sb.delete(0, sb.length());
    			} else if (isType) {
    				sb.append(c);
    			}
    		}
    	}
    	ss[2] = sb.toString();
    	return ss;
    }
}
