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


import org.apache.http.HttpHost;
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
import org.olat.core.util.StringHelper;
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
		return createHttpClientBuilder(null, -1, null, null);
	}
	
	@Override
	public HttpClientBuilder createHttpClientBuilder(String host, int port, String user, String password) {
		dbInstance.commit();// free connection
		
		RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
				.setConnectTimeout(httpClientModule.getHttpConnectTimeout())
				.setConnectionRequestTimeout(httpClientModule.getHttpConnectRequestTimeout())
				.setSocketTimeout(httpClientModule.getHttpSocketTimeout())
				.build();
		HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
		
		setProxyAndCredentials(builder, host, port, user, password);
		
		return builder;
	}
	
	@Override
	public CloseableHttpClient createHttpClient() {
		return createHttpClientBuilder().build();
	}
	
	@Override
	public CloseableHttpClient createThreadSafeHttpClient(boolean redirect) {
		return createThreadSafeHttpClient(null, -1, null, null, redirect);
	}

	@Override
	public CloseableHttpClient createThreadSafeHttpClient(String host, int port, String user, String password, boolean redirect) {
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
		
		setProxyAndCredentials(clientBuilder, host, port, user, password);
		
		return clientBuilder.build();
	}

	private void setProxyAndCredentials(HttpClientBuilder builder, String host, int port, String user,
			String password) {
		CredentialsProvider credentialsProvider = null;
		if (StringHelper.containsNonWhitespace(httpClientModule.getHttpProxyUrl())) {
			HttpHost proxy = new HttpHost(httpClientModule.getHttpProxyUrl(), httpClientModule.getHttpProxyPort());
			builder.setProxy(proxy);
			builder.setRoutePlanner(new ProxyRoutePlanner(proxy, httpClientModule.getHttpProxyExclusionUrls()));
			
			if (StringHelper.containsNonWhitespace(httpClientModule.getHttpProxyUser()) && StringHelper.containsNonWhitespace(httpClientModule.getHttpProxyPwd())) {
				credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(
						new AuthScope(httpClientModule.getHttpProxyUrl(), httpClientModule.getHttpProxyPort()),
						new UsernamePasswordCredentials(httpClientModule.getHttpProxyUser(), httpClientModule.getHttpProxyPwd())
					);
			}
		}
		
		if (StringHelper.containsNonWhitespace(host) && StringHelper.containsNonWhitespace(user) && StringHelper.containsNonWhitespace(password)) {
			if (credentialsProvider == null) {
				credentialsProvider = new BasicCredentialsProvider();
			}
			credentialsProvider.setCredentials(
					new AuthScope(host, port),
					new UsernamePasswordCredentials(user, password)
				);
		}
		
		builder.setDefaultCredentialsProvider(credentialsProvider);
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
