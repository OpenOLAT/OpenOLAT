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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.filter.FilteringDnsResolver;
import org.olat.core.util.httpclient.filter.FilteringHostRequestInterceptor;
import org.olat.core.util.httpclient.filter.InetAddressFilter;
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
	public HttpClientBuilder createHttpClientBuilder(ProtectionProfile profile) {
		return createHttpClientBuilder(null, -1, null, null, profile);
	}

	@Override
	public HttpClientBuilder createHttpClientBuilder(String host, int port, String user, String password, ProtectionProfile profile) {
		dbInstance.commit();// free connection
		
		RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
				.setConnectTimeout(httpClientModule.getHttpConnectTimeout())
				.setConnectionRequestTimeout(httpClientModule.getHttpConnectRequestTimeout())
				.setSocketTimeout(httpClientModule.getHttpSocketTimeout())
				.build();
		HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);

		setProxyAndCredentials(builder, host, port, user, password);
		if(isSsrfProtectionActive(profile)) {
			// Only effective as long as no connection manager is set on the builder
			builder.setDnsResolver(createDnsResolver())
				.addInterceptorFirst(createHostRequestInterceptor());
		}

		return builder;
	}

	@Override
	public CloseableHttpClient createHttpClient(ProtectionProfile profile) {
		return createHttpClientBuilder(profile).build();
	}

	@Override
	public CloseableHttpClient createThreadSafeHttpClient(boolean redirect, ProtectionProfile profile) {
		return createThreadSafeHttpClient(null, -1, null, null, redirect, profile);
	}

	@Override
	public CloseableHttpClient createThreadSafeHttpClient(String host, int port, String user, String password, boolean redirect, ProtectionProfile profile) {
		dbInstance.commit();// free connection
		
		boolean filtered = isSsrfProtectionActive(profile);
		PoolingHttpClientConnectionManager cm = filtered
				? new PoolingHttpClientConnectionManager(socketFactoryRegistry(), createDnsResolver())
				: new PoolingHttpClientConnectionManager();
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
		if(filtered) {
			clientBuilder.addInterceptorFirst(createHostRequestInterceptor());
		}

		return clientBuilder.build();
	}

	private boolean isSsrfProtectionActive(ProtectionProfile profile) {
		return profile == ProtectionProfile.USER_PROVIDED && httpClientModule.isSsrfProtectionEnabled();
	}
	
	private HttpRequestInterceptor createHostRequestInterceptor() {
		return new FilteringHostRequestInterceptor(createSsrfAddressFilter(), httpClientModule.getSsrfAllowedHostsList());
	}

	/**
	 * The resolver is the last step before the socket is opened and the addresses it
	 * returns are the ones HttpClient connects to. Filtering here is therefore immune to
	 * DNS rebinding and covers every hop of a redirect chain.
	 *
	 * @return A DNS resolver which rejects the filtered addresses
	 */
	private DnsResolver createDnsResolver() {
		InetAddressFilter filter = createSsrfAddressFilter();
		Set<String> exemptedHosts = new HashSet<>(httpClientModule.getSsrfAllowedHostsList());
		// The proxy itself is typically an internal host, its address must not be filtered
		if(StringHelper.containsNonWhitespace(httpClientModule.getHttpProxyUrl())) {
			exemptedHosts.add(httpClientModule.getHttpProxyUrl());
		}
		return new FilteringDnsResolver(filter, exemptedHosts);
	}

	private static Registry<ConnectionSocketFactory> socketFactoryRegistry() {
		return RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", SSLConnectionSocketFactory.getSocketFactory())
				.build();
	}
	
	public InetAddressFilter createSsrfAddressFilter() {
		List<String> allowedAddresses = httpClientModule.getSsrfAllowedAddressesList();
		InetAddressFilter filter = InetAddressFilter.externalAddresses();
		if(!allowedAddresses.isEmpty()) {
			filter = filter.or(allowedAddresses.toArray(new String[allowedAddresses.size()]));
		}
		return filter;
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
