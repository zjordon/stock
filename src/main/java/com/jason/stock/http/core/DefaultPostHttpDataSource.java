/**
 * 
 */
package com.jason.stock.http.core;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * @author lenovo
 *
 */
public class DefaultPostHttpDataSource extends AbstractHttpDataSource {

	public DefaultPostHttpDataSource(String httpUrl,
			Map<String, String> paramMap, int timeout) {
		super(httpUrl, paramMap, timeout);
	}

	/* (non-Javadoc)
	 * @see com.jason.stock.http.core.AbstractHttpDataSource#initRequestBaseObj()
	 */
	@Override
	protected void initRequestBaseObj() throws DataAccessException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Iterator<Map.Entry<String, String>> iter = paramMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		super.httpRequest = new HttpPost(httpUrl);
		try {
			((HttpEntityEnclosingRequestBase)super.httpRequest).setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new DataAccessException("exception happend", e);
		}

	}

}
