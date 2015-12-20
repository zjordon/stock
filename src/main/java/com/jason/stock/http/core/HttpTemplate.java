/**
 * 
 */
package com.jason.stock.http.core;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author jasonzhang
 *
 */
public class HttpTemplate extends HttpAccessor implements HttpOperations {
	
	public HttpTemplate(HttpDataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	public <T> T execute(HttpUriRequestCallback<T> action) throws DataAccessException {
		HttpRequestBase request = null;
		try {
			request = super.getDataSource().getHttpUriRequest();
			return action.doInHttpUriRequest(request);
		} catch (IOException ioe) {
			super.logger.error("exception happend", ioe);
			throw new DataAccessException(ioe.getMessage());
		} finally {
			if (request != null) {
				request.releaseConnection();
			}
		}
	}

	@Override
	public <T> T query(final CloseableHttpResponseExtractor<T> rse)
			throws DataAccessException {
		return execute(new HttpUriRequestCallback<T>(){

			@Override
			public T doInHttpUriRequest(HttpRequestBase request)
					throws IOException {
				CloseableHttpClient httpclient = null;
				CloseableHttpResponse rs = null;
				try {
					httpclient= HttpClients.createDefault();
					rs = httpclient.execute(request);
					return rse.extractData(rs);
				} finally {
					if (httpclient != null) {
						httpclient.close();
					}
					if (rs != null) {
						rs.close();
					}
				}
			}
			
		});
	}

}
