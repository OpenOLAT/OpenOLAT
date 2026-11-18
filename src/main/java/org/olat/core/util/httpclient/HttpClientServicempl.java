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

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.filter.FilteringDnsResolver;
import org.olat.core.util.httpclient.filter.FilteringHostRequestInterceptor;
import org.olat.core.util.httpclient.filter.InetAddressFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The factory of all the outgoing HTTP clients of OpenOlat (Apache HttpClient 5).
 * <p>
 * The builders always come with their own connection manager. In HttpClient 5,
 * the DNS resolver of the SSRF protection and the connect / socket timeouts are
 * settings of the connection manager, there is no way to set them on the builder.
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
		return createBuilder(host, port, user, password, isSsrfProtectionActive(profile));
	}

	@Override
	public CloseableHttpClient createHttpClient(ProtectionProfile profile) {
		return createHttpClientBuilder(profile).build();
	}

	@Override
	public CloseableHttpClient createThreadSafeHttpClient(boolean redirect, ProtectionProfile profile) {
		return createThreadSafeHttpClient(null, -1, null, null, redirect, profile);
	}

	/**
	 * The default redirect strategy of HttpClient 5 follows the redirects of every
	 * method, as the {@code LaxRedirectStrategy} of HttpClient 4 did.
	 */
	@Override
	public CloseableHttpClient createThreadSafeHttpClient(String host, int port, String user, String password, boolean redirect, ProtectionProfile profile) {
		dbInstance.commit();// free connection
		
		HttpClientBuilder builder = createBuilder(host, port, user, password, isSsrfProtectionActive(profile))
				.setDefaultCookieStore(new BasicCookieStore());
		if(!redirect) {
			builder.disableRedirectHandling();
		}
		return builder.build();
	}
	
	private HttpClientBuilder createBuilder(String host, int port, String user, String password, boolean ssrfProtection) {
		ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom();
		if(httpClientModule.getHttpConnectTimeout() >= 0) {
			connectionConfig.setConnectTimeout(Timeout.ofMilliseconds(httpClientModule.getHttpConnectTimeout()));
		}
		if(httpClientModule.getHttpSocketTimeout() >= 0) {
			connectionConfig.setSocketTimeout(Timeout.ofMilliseconds(httpClientModule.getHttpSocketTimeout()));
		}
		
		PoolingHttpClientConnectionManagerBuilder cmBuilder = PoolingHttpClientConnectionManagerBuilder.create()
				.setDefaultConnectionConfig(connectionConfig.build());
		if(ssrfProtection) {
			cmBuilder.setDnsResolver(createDnsResolver());
		}
		
		RequestConfig.Builder requestConfig = RequestConfig.custom();
		if(httpClientModule.getHttpConnectRequestTimeout() >= 0) {
			requestConfig.setConnectionRequestTimeout(Timeout.ofMilliseconds(httpClientModule.getHttpConnectRequestTimeout()));
		}
		if(httpClientModule.getHttpSocketTimeout() >= 0) {
			requestConfig.setResponseTimeout(Timeout.ofMilliseconds(httpClientModule.getHttpSocketTimeout()));
		}

		HttpClientBuilder builder = HttpClientBuilder.create()
				.setConnectionManager(cmBuilder.build())
				.setDefaultRequestConfig(requestConfig.build());
		setProxyAndCredentials(builder, host, port, user, password);
		if(ssrfProtection) {
			builder.addRequestInterceptorFirst(createHostRequestInterceptor());
		}
		return builder;
	}

	private boolean isSsrfProtectionActive(ProtectionProfile profile) {
		return profile == ProtectionProfile.USER_PROVIDED && httpClientModule.isSsrfProtectionEnabled();
	}
	
	private FilteringHostRequestInterceptor createHostRequestInterceptor() {
		return new FilteringHostRequestInterceptor(createSsrfAddressFilter(), httpClientModule.getSsrfAllowedHostsList());
	}

	/**
	 * The resolver is the last step before the socket is opened and the addresses it
	 * returns are the ones HttpClient connects to. Filtering here is therefore immune to
	 * DNS rebinding and covers every hop of a redirect chain.
	 *
	 * @return A DNS resolver which rejects the filtered addresses
	 */
	private FilteringDnsResolver createDnsResolver() {
		Set<String> exemptedHosts = new HashSet<>(httpClientModule.getSsrfAllowedHostsList());
		// The proxy itself is typically an internal host, its address must not be filtered
		if(StringHelper.containsNonWhitespace(httpClientModule.getHttpProxyUrl())) {
			exemptedHosts.add(httpClientModule.getHttpProxyUrl());
		}
		return new FilteringDnsResolver(createSsrfAddressFilter(), exemptedHosts);
	}
	
	private InetAddressFilter createSsrfAddressFilter() {
		List<String> allowedAddresses = httpClientModule.getSsrfAllowedAddressesList();
		InetAddressFilter filter = InetAddressFilter.externalAddresses();
		if(!allowedAddresses.isEmpty()) {
			filter = filter.or(allowedAddresses.toArray(new String[allowedAddresses.size()]));
		}
		return filter;
	}

	private void setProxyAndCredentials(HttpClientBuilder builder, String host, int port, String user, String password) {
		BasicCredentialsProvider credentialsProvider = null;
		String proxyHost = httpClientModule.getHttpProxyUrl();
		if (StringHelper.containsNonWhitespace(proxyHost)) {
			int proxyPort = httpClientModule.getHttpProxyPort();
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			// The route planner takes precedence over setProxy()
			builder.setRoutePlanner(new ProxyRoutePlanner(proxy, httpClientModule.getHttpProxyExclusionUrls()));
			
			String proxyUser = httpClientModule.getHttpProxyUser();
			String proxyPassword = httpClientModule.getHttpProxyPwd();
			if (StringHelper.containsNonWhitespace(proxyUser) && StringHelper.containsNonWhitespace(proxyPassword)) {
				credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
						new UsernamePasswordCredentials(proxyUser, proxyPassword.toCharArray()));
			}
		}
		
		if (StringHelper.containsNonWhitespace(host) && StringHelper.containsNonWhitespace(user) && StringHelper.containsNonWhitespace(password)) {
			if (credentialsProvider == null) {
				credentialsProvider = new BasicCredentialsProvider();
			}
			credentialsProvider.setCredentials(new AuthScope(host, port),
					new UsernamePasswordCredentials(user, password.toCharArray()));
		}
		
		if(credentialsProvider != null) {
			builder.setDefaultCredentialsProvider(credentialsProvider);
		}
	}
}
