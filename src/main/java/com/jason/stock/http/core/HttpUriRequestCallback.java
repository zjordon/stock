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
public interface HttpUriRequestCallback<T> {

	T doInHttpUriRequest(HttpRequestBase request) throws IOException;
}
