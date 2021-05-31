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


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial Date: 21.03.2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
@Service
public class HttpClientServicempl implements HttpClientService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private HttpClientModule httpClientModule;
	
	@Override
	public HttpClientBuilder createHttpClientBuilder() {
		dbInstance.commit();// free connection
		
		RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
				.setConnectTimeout(httpClientModule.getHttpConnectTimeout())
				.setConnectionRequestTimeout(httpClientModule.getHttpConnectRequestTimeout())
				.setSocketTimeout(httpClientModule.getHttpSocketTimeout())
				.build();
		return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
	}
	
	@Override
	public CloseableHttpClient createHttpClient() {
		return createHttpClientBuilder().build();
	}
	
	@Override
	public CloseableHttpClient getThreadSafeHttpClient(boolean redirect) {
		return getThreadSafeHttpClient(null, -1, null, null, redirect);
	}

	@Override
	public CloseableHttpClient getThreadSafeHttpClient(String host, int port, String user, String password, boolean redirect) {
		dbInstance.commit();// free connection
		
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		SocketConfig.Builder socketConfigBuilder = SocketConfig.copy(SocketConfig.DEFAULT);
		socketConfigBuilder.setSoTimeout(httpClientModule.getHttpSocketTimeout());
		cm.setDefaultSocketConfig(socketConfigBuilder.build());

		HttpClientBuilder clientBuilder = HttpClientBuilder.create()
				.setConnectionManager(cm).setMaxConnTotal(10)
				.setDefaultCookieStore(new BasicCookieStore());
		if(redirect) {
			clientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
		} else {
			clientBuilder.setRedirectStrategy(new NoRedirectStrategy());
		}
		if (user != null && user.length() > 0) {
			CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(user, password));
			clientBuilder.setDefaultCredentialsProvider(provider);
		}
		
		return clientBuilder.build();
	}
	
	private static class NoRedirectStrategy implements RedirectStrategy {
		@Override
		public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
			return false;
		}

		@Override
		public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
			return null;
		}
	}
	
}
