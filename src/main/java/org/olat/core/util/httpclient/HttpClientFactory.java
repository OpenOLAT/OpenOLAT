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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * <h3>Description:</h3>
 * The HttpClientFactory creates multithreaded jakarta commons HttpClients that
 * feature SSL capability with support for unsigned SSL certificates. <br>
 * When using HttpClient in OLAT you must use this factory and never use new
 * HttpClient() directly since this would not be thread save.
 * <p>
 * Initial Date: 21.03.2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class HttpClientFactory {
	static {
		// register https as an available protocol using the SSL socket factory that
		// accepts self signed certificates
		Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
	}
	
	/**
	 * [used by Spring]
	 */
	public void destroy() {
		MultiThreadedHttpConnectionManager.shutdownAll();
	}

	/**
	 * A HttpClient without basic authentication and no host or port setting. Can
	 * only be used to retrieve absolute URLs
	 * 
	 * @return HttpClient
	 */
	public static HttpClient getHttpClientInstance() {
		return getHttpClientInstance(null, null);
	}

	/**
	 * A HttpClient with basic authentication and no host or port setting. Can
	 * only be used to retrieve absolute URLs
	 * 
	 * @param user can be NULL
	 * @param password can be NULL
	 * @return HttpClient
	 */
	public static HttpClient getHttpClientInstance(String user, String password) {
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionParams params = connectionManager.getParams();
		// wait max 10 seconds to establish connection
		params.setConnectionTimeout(10000);
		// a read() call on the InputStream associated with this Socket
		// will block for only this amount
		params.setSoTimeout(10000);
		HttpClient c = new HttpClient(connectionManager);

		// use basic authentication if available
		if (user != null && user.length() > 0) {
			AuthScope authScope = new AuthScope(null, -1, null);
			Credentials credentials = new UsernamePasswordCredentials(user, password);
			c.getState().setCredentials(authScope, credentials);
		}
		return c;
	}

	/**
	 * A HttpClient with basic authentication and host or port setting. Can only
	 * be used to retrieve relative URLs
	 * 
	 * @param host must not be NULL
	 * @param port must not be NULL
	 * @param protocol must not be NULL
	 * @param user can be NULL
	 * @param password can be NULL
	 * @return HttpClient
	 */
	public static HttpClient getHttpClientInstance(String host, int port, String protocol, String user, String password) {
		HttpClient c = getHttpClientInstance(user, password);
		c.getHostConfiguration().setHost(host, port, protocol);
		return c;
	}

}
