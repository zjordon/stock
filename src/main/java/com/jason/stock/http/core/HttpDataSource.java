/**
 * 
 */
package com.jason.stock.http.core;

import java.io.IOException;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * @author jasonzhang
 *
 */
public interface HttpDataSource {

	HttpRequestBase getHttpUriRequest() throws IOException;
}
