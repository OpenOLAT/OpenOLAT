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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.httpclient.filter.IpAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 31.05.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class HttpClientModule extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(HttpClientModule.class);
	
	public static final String HTTP_SSRF_PROTECTION_ENABLED_KEY = "http.ssrf.protection.enabled";

	@Value("${http.connect.timeout:30000}")
	private int httpConnectTimeout;
	@Value("${http.connect.request.timeout:30000}")
	private int httpConnectRequestTimeout;
	@Value("${http.connect.socket.timeout:30000}")
	private int httpSocketTimeout;
	@Value("${http.proxy.url}")
	private String httpProxyUrl;
	@Value("${http.proxy.port:8080}")
	private int httpProxyPort;
	@Value("${http.proxy.exclusion}")
	private String httpProxyExclusion;
	private Set<String> httpProxyExclusionUrls;
	@Value("${http.proxy.user}")
	private String httpProxyUser;
	@Value("${http.proxy.pwd}")
	private String httpProxyPwd;
	@Value("${http.ssrf.protection.enabled:true}")
	private String ssrfProtectionEnabled;
	@Value("${http.ssrf.allowed.addresses}")
	private String ssrfAllowedAddresses;
	private List<String> ssrfAllowedAddressesList;
	@Value("${http.ssrf.allowed.hosts}")
	private String ssrfAllowedHosts;

	@Autowired
	private HttpClientModule(CoordinatorManager coordinateManager) {
		super(coordinateManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		ssrfProtectionEnabled = getStringPropertyValue(HTTP_SSRF_PROTECTION_ENABLED_KEY, ssrfProtectionEnabled);
		
		// Validate the addresses
		ssrfAllowedAddressesList = StringHelper.containsNonWhitespace(ssrfAllowedAddresses)
				? Arrays.stream(ssrfAllowedAddresses.split(","))
						.map(String::trim)
						.map(addr -> {
							try {
								IpAddress.of(addr);
								return addr;
							} catch(IllegalArgumentException e) {
								log.error("Cannot parse IP address: {}, will be ignored", addr, e);
							}
							return null;
						}) 
						.filter(StringHelper::containsNonWhitespace)
						.toList()
				: List.of();
	}

	public int getHttpConnectTimeout() {
		return httpConnectTimeout;
	}

	public int getHttpConnectRequestTimeout() {
		return httpConnectRequestTimeout;
	}

	public int getHttpSocketTimeout() {
		return httpSocketTimeout;
	}

	public String getHttpProxyUrl() {
		return httpProxyUrl;
	}

	public int getHttpProxyPort() {
		return httpProxyPort;
	}

	public Set<String> getHttpProxyExclusionUrls() {
		if (httpProxyExclusionUrls == null) {
			if (StringHelper.containsNonWhitespace(httpProxyExclusion)) {
				httpProxyExclusionUrls = Arrays.asList(httpProxyExclusion.split(",")).stream()
						.filter(StringHelper::containsNonWhitespace)
						.map(String::toLowerCase)
						.collect(Collectors.toSet());
			} else {
				httpProxyExclusionUrls = Set.of();
			}
		}
		return httpProxyExclusionUrls;
	}

	public String getHttpProxyUser() {
		return httpProxyUser;
	}

	public String getHttpProxyPwd() {
		return httpProxyPwd;
	}
	
	public boolean isSsrfProtectionEnabled() {
		return "true".equals(ssrfProtectionEnabled);
	}
	
	public void setSsrfProtectionEnabled(boolean enabled) {
		ssrfProtectionEnabled = enabled ? "true" : "false";
		setStringProperty(HTTP_SSRF_PROTECTION_ENABLED_KEY, ssrfProtectionEnabled, true);
	}

	public String getSsrfAllowedAddresses() {
		return ssrfAllowedAddresses;
	}
	
	public List<String> getSsrfAllowedAddressesList() {
		return ssrfAllowedAddressesList;
	}

	public String getSsrfAllowedHosts() {
		return ssrfAllowedHosts;
	}
	
	public List<String> getSsrfAllowedHostsList() {
		return parseLists(ssrfAllowedHosts);
	}
	
	private List<String> parseLists(String allowedString) {
		return StringHelper.containsNonWhitespace(allowedString)
				? Arrays.stream(allowedString.split(","))
						.map(String::trim)
						.filter(StringHelper::containsNonWhitespace)
						.toList()
				: List.of();
	}
	
}
