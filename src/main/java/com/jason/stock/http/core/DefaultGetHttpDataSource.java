/**
 * 
 */
package com.jason.stock.http.core;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

/**
 * @author lenovo
 *
 */
public class DefaultGetHttpDataSource extends AbstractHttpDataSource {

	public DefaultGetHttpDataSource(String httpUrl,
			Map<String, String> paramMap, int timeout) {
		super(httpUrl, paramMap, timeout);
	}

	/* (non-Javadoc)
	 * @see com.jason.stock.http.core.AbstractHttpDataSource#initRequestBaseObj()
	 */
	@Override
	protected void initRequestBaseObj() throws DataAccessException {
		URIBuilder urlBuilder = null;
		try {
			urlBuilder = new URIBuilder(httpUrl);
			Iterator<Map.Entry<String, String>> iter = paramMap.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				urlBuilder.setParameter(entry.getKey(), entry.getValue());
			}
			super.httpRequest = new HttpGet(urlBuilder.build());
		} catch (URISyntaxException e) {
			throw new DataAccessException("exception happend", e);
		}
		

	}

}
