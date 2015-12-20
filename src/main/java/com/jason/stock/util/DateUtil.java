/**
 * 
 */
package com.jason.stock.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author jasonzhang
 *
 */
public class DateUtil {

	public final static List<int[]> getAllWeek(int startDate, int endDate) {
    	List<int[]> list = new ArrayList<int[]>();
    	Calendar startCal = Calendar.getInstance();
    	int[] startYmd = getYMD(startDate);
    	startCal.set(startYmd[0], startYmd[1] - 1, startYmd[2]);
    	int temp = 0;
    	int[] week = new int[2];
    	for (;isLt(startCal, endDate);startCal.add(Calendar.DAY_OF_YEAR, 1)) {
    		temp = startCal.get(Calendar.DAY_OF_WEEK);
    		if (temp == Calendar.MONDAY) {
    			week[0] = getYMDInt(startCal);
    		} else if (temp == Calendar.FRIDAY) {
    			week[1] = getYMDInt(startCal);
    			list.add(week);
    			week = new int[2];
    		}
    	}
    	return list;
    }
	
	public final static int getStartDate(int endDate, int dayNum) {
		int year = endDate/10000;
		int month = (endDate - year*10000)/100;
		int day = endDate - year*10000 - month*100;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		for (int i=0; i<dayNum;) {
//			cal.roll(Calendar.DAY_OF_MONTH, -1);
			cal.add(Calendar.DATE, -1);
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				continue;
			} else {
//				System.out.println(getYMDInt(cal));
				i++;
			}
		}
		return getYMDInt(cal);
	}
	
	private final static int[] getYMD(int date) {
		int year = date/10000;
		int month = (date - year*10000)/100;
		int day = date - year*10000 - month*100;
		int[] ymd = new int[]{year, month, day};
		return ymd;
	}
	
	private final static boolean isLt(Calendar cal, int endDate) {
		return getYMDInt(cal) <= endDate;
	}
	
	private final static int getYMDInt(Calendar cal) {
		return cal.get(Calendar.YEAR)*10000 + (cal.get(Calendar.MONTH) + 1)*100 + cal.get(Calendar.DAY_OF_MONTH);
	}
}
