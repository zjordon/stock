/**
 * 
 */
package com.jason.stock.http.core;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;

/**
 * @author jasonzhang
 *
 */
public interface CloseableHttpResponseExtractor<T> {

	T extractData(CloseableHttpResponse rs) throws IOException, DataAccessException;
}
