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

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

/**
 * 
 * Initial date: 31 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface HttpClientService {
	
	/**
	 * Creates a builder with the default configuration (timeouts, proxy and SSRF
	 * protection). The builder comes with its own connection manager which holds
	 * the timeouts and the filtering DNS resolver: don't replace it with
	 * {@code setConnectionManager}, it would disable the SSRF protection.
	 * 
	 * @param profile Profile to activate SSRF protection
	 */
	HttpClientBuilder createHttpClientBuilder(ProtectionProfile profile);
	
	/**
	 * Same as {@link #createHttpClientBuilder(ProtectionProfile)} with basic authentication.
	 *
	 * @param host For basic authentication
	 * @param port For basic authentication
	 * @param user For basic authentication
	 * @param password For basic authentication
	 * @param profile Profile to activate SSRF protection
	 */
	HttpClientBuilder createHttpClientBuilder(String host, int port, String user, String password, ProtectionProfile profile);
	
	/**
	 * Creates a client with the default configuration.
	 * 
	 * @param profile Profile to activate SSRF protection
	 */
	CloseableHttpClient createHttpClient(ProtectionProfile profile);
	
	/**
	 * Creates a thread safe client with a cookie store.
	 * 
	 * @param redirect If redirect is allowed
	 * @param profile Profile to activate SSRF protection
	 * @return CloseableHttpClient
	 */
	CloseableHttpClient createThreadSafeHttpClient(boolean redirect, ProtectionProfile profile);
	
	/**
	 * Creates a thread safe client with a cookie store.
	 * 
	 * @param host For basic authentication
	 * @param port For basic authentication
	 * @param user For basic authentication
	 * @param password For basic authentication
	 * @param redirect If redirect is allowed
	 * @param profile Profile to activate SSRF protection
	 * @return CloseableHttpClient
	 */
	CloseableHttpClient createThreadSafeHttpClient(String host, int port, String user, String password, boolean redirect, ProtectionProfile profile);

}
