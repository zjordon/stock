/**
 * 
 */
package com.jason.stock.http.core;

/**
 * @author jasonzhang
 *
 */
public interface HttpOperations {

	<T> T execute(HttpUriRequestCallback<T> action) throws DataAccessException;
	
	<T> T query(CloseableHttpResponseExtractor<T> rse) throws DataAccessException;
}
