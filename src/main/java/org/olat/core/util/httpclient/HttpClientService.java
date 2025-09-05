/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.httpclient;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * 
 * Initial date: 31 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface HttpClientService {
	
	/*
	 * Creates a HttpClientBuilder with default configuration.
	 */
	public HttpClientBuilder createHttpClientBuilder();
	
	/*
	 * Creates a HttpClientBuilder with default configuration.
	 *
	 * @param host For basic authentication
	 * @param port For basic authentication
	 * @param port For basic authentication
	 * @param user For basic authentication
	 */
	public HttpClientBuilder createHttpClientBuilder(String host, int port, String user, String password);
	
	/*
	 * Creates a HttpClient with default configuration.
	 */
	public CloseableHttpClient createHttpClient();
	
	/**
	 * Creates a thread safe http client.
	 * 
	 * @param redirect If redirect is allowed
	 * @return CloseableHttpClient
	 */
	public CloseableHttpClient createThreadSafeHttpClient(boolean redirect);
	
	/**
	 * Creates a thread safe http client.
	 * 
	 * @param host For basic authentication
	 * @param port For basic authentication
	 * @param user For basic authentication
	 * @param password For basic authentication
	 * @param redirect If redirect is allowed
	 * @return CloseableHttpClient
	 */
	public CloseableHttpClient createThreadSafeHttpClient(String host, int port, String user, String password, boolean redirect);
	
	

}
