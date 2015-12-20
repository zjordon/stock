/**
 * 
 */
package com.jason.stock.http.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jasonzhang
 *
 */
public abstract class HttpAccessor {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	private HttpDataSource dataSource;

	public HttpDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(HttpDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	
}
