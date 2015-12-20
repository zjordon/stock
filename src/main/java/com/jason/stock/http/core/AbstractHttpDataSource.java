/**
 * 
 */
package com.jason.stock.http.core;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * @author lenovo
 *
 */
public abstract class AbstractHttpDataSource implements HttpDataSource {
	
	protected HttpRequestBase httpRequest;
	
	protected int timeout;
	protected String httpUrl;
	protected Map<String, String> paramMap;
	
	public AbstractHttpDataSource(String httpUrl, Map<String, String> paramMap, int timeout) {
		this.httpUrl = httpUrl;
		this.paramMap = paramMap;
		this.timeout = timeout;
	}
	
	public void init() throws DataAccessException{
		this.initRequestBaseObj();
		this.initRequestConfig();
	}
	
	protected abstract void initRequestBaseObj() throws DataAccessException;
	protected void initRequestConfig() {
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).setSocketTimeout(timeout).build();
		httpRequest.setConfig(requestConfig);
	}

	/* (non-Javadoc)
	 * @see com.jason.stock.http.core.HttpDataSource#getHttpUriRequest()
	 */
	@Override
	public HttpRequestBase getHttpUriRequest() throws IOException {
		return this.httpRequest;
	}

}
